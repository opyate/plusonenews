package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import models.Get
import akka.actor.ActorLogging

object Receptionist {
  private case class Job(client: ActorRef, url: String)
  case class Result(url: String, links: Set[String])
  case class Failed(url: String)
}

class Receptionist extends Actor with ActorLogging {
  import Receptionist._
  
  def controllerProps: Props = Props[Controller]

  var reqNo = 0

  def receive = waiting

  val waiting: Receive = {
    case get @ Get(id, link) =>
      log.debug("Receptionist received {}", get)
      context.become(runNext(Vector(Job(sender, link.url))))
  }

  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      context.stop(sender)
      context.become(runNext(queue.tail))
    case Get(id, link) =>
      context.become(enqueueJob(queue, Job(sender, link.url)))
  }

  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if (queue.isEmpty) waiting
    else {
      val controller = context.actorOf(controllerProps, s"controller-$reqNo")
      controller ! Controller.Check(queue.head.url, 2)
      running(queue)
    }
  }

  def enqueueJob(queue: Vector[Job], job: Job): Receive = {
    if (queue.size > 3) {
      sender ! Failed(job.url)
      running(queue)
    } else running(queue :+ job)
  }

}
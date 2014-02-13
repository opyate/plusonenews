package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import models._
import akka.actor.ActorLogging

object Receptionist {
  // TODO set based on certain metrics
  val JobLimit = 3
  
  private case class Job(client: ActorRef, website: Website)
  case class Result(website: Website, corpus: String)
  case class Failed(website: Website)
}

class Receptionist extends Actor with ActorLogging {
  import Receptionist._
  
  def controllerProps(website: Website): Props = Props(new Controller(website))

  var reqNo = 0
  
  var children = Set.empty[ActorRef]

  def receive = waiting

  val waiting: Receive = {
    case get @ Get(id, website) =>
      log.debug("Receptionist received {}", get)
      context.become(runNext(Vector(Job(sender, website))))
  }

  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(website, corpus) =>
      val job = queue.head
      job.client ! Result(job.website, corpus)
      children -= sender
      if (children.isEmpty) {
        log.debug("All Children done")
      }
      context.stop(sender)
      context.become(runNext(queue.tail))
    case Get(id, website) =>
      context.become(enqueueJob(queue, Job(sender, website)))
  }

  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if (queue.isEmpty) waiting
    else {
      children +=  context.actorOf(controllerProps(queue.head.website), s"controller-$reqNo")
      
      running(queue)
    }
  }

  // since we don't want to bombard our friends with hits, we limit ourselves to only a few scrape jobs
  def enqueueJob(queue: Vector[Job], job: Job): Receive = {
    if (queue.size > JobLimit) {
      sender ! Failed(job.website)
      running(queue)
    } else running(queue :+ job)
  }

}
package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.SupervisorStrategy
import akka.actor.ActorLogging
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import akka.actor.ActorRef
import models.Website
import akka.actor.Actor
import akka.pattern.pipe
import java.util.concurrent.Executor
import akka.actor.ActorLogging
import akka.actor.Status
import scala.concurrent.ExecutionContext
import spray.http._
import spray.client.pipelining._
import scala.concurrent.Future
import models.Website

object Controller {
  case class Result(website: Website, corpus: String)
  case object Abort
}

class Controller(website: Website) extends Actor with ActorLogging {
  import Controller._
  
  context.setReceiveTimeout(10.seconds)
  
  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  log.warning("Spawning a worker for url {}", website.url)
  
  pipeline(Get(website.url)) pipeTo self
  
  def receive = {
    case response: HttpResponse =>
      val body = response.entity.asString
      val corpus = extract(body, Set.empty[String])
      context.parent ! Result(website, corpus)
      context.stop(self)
    case _: Status.Failure => self ! Abort
    case ReceiveTimeout => self ! Abort
    case Abort => context.stop(self)
    case x =>
      log.error("Unknown message {}", x)
      self ! Abort
  }
  

  def extract(body: String, xpath: Set[String]): String = {
    // TODO extract the data at 'xpath'
    body
  }
}
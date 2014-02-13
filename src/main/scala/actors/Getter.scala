package actors

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

object Getter {
  case object Done
  case object Abort
}

class Getter(website: Website, depth: Int) extends Actor with ActorLogging {
  import Getter._

  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  log.warning("Spawning a worker for url {}", website.url)
  
  pipeline(Get(website.url)) pipeTo self

  def receive = {
    case response: HttpResponse =>
      val body = response.entity.asString
      for (link <- extract(body, Set.empty[String]))
        context.parent ! Controller.Check(website, depth)
      stop()
    case _: Status.Failure => stop()
    case Abort             => stop()
    case x =>
      log.error("Unknown message {}", x)
      stop()
  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }

  def extract(body: String, xpath: Set[String]): Iterable[String] = {
    // TODO extract the data at 'xpath'
    body :: Nil
  }

}
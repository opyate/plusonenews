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
import models.Id
import org.jsoup.Jsoup
import org.jsoup.select.Elements

object Scraper {
  case class Abort(jobId: Id)
}

class Scraper(jobId: Id, website: Website) extends Actor with ActorLogging {
  import ScrapeReceiver.Result
  import Scraper._
  
  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  log.warning("Spawning a scraper for url {}", website.url)
  
  pipeline(Get(website.url)) pipeTo self
  
  def receive = {
    case response: HttpResponse =>
      val body = response.entity.asString
      val corpus = extract(body, getSelectorFor(website))
      context.parent ! Result(jobId, website, corpus)
      context.stop(self)
    case _: Status.Failure => self ! Abort(jobId)
    case ReceiveTimeout => self ! Abort(jobId)
    case abort: Abort =>
      context.parent ! abort
      context.stop(self)
    case x =>
      log.error("Unknown message {}", x)
      self ! Abort(jobId)
  }
  

  def extract(body: String, selector: String): String = {
    val doc = Jsoup.parse(body)
    val elem: Elements = doc.select(selector)
    elem.text()
  }
  
  // TODO move this elsewhere
  def getSelectorFor(website: Website) = website.url match {
    case s: String if (s.contains("businessinsider.com")) => "#content"
    case s: String if (s.contains("buzzfeed.com")) => ".buzz > div:nth-child(5)"
    case _ => "body"
  }
}
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

object Getter {
  case object Done
  case object Abort
}

class Getter(url: String, depth: Int) extends Actor with ActorLogging {
  import Getter._

  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  log.warning("Spawning a worker for url {}", url)
  
  pipeline(Get(url)) pipeTo self

  def receive = {
    case response: HttpResponse =>
      val body = response.entity.asString
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
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

  val A_TAG = "(?i)<a ([^>]+)>.+?</a>".r
  val HREF_ATTR = """\s*(?i)href\s*=\s*(?:"([^"]*)"|'([^']*)'|([^'">\s]+))\s*""".r

  def findLinks(body: String): Iterator[String] = {
    for {
      anchor <- A_TAG.findAllMatchIn(body)
      HREF_ATTR(dquot, quot, bare) <- anchor.subgroups
    } yield if (dquot != null) dquot
    else if (quot != null) quot
    else bare
  }

}
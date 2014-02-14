package actors

import akka.actor.ActorLogging
import akka.actor.Actor
import models.Website
import models.Id
import spray.http._
import spray.client.pipelining._
import spray.http.HttpRequest
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import akka.pattern.pipe

object PublishActor {
  
}

/**
 * I apologise if your eyes bleed, but I wanted to get this done today (this, and beingjohnmarkovic, and linkbaiter, and the publisher thingy)
 */
class PublishActor(jobId: Id, website: Website, corpus: String) extends Actor with ActorLogging {

  import JsonProtocol.markovicFormat
  import spray.httpx.SprayJsonSupport._
  
  
  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  
  val markovicCorpusAPI = System.getenv("MARKOVIC") + "corpus"
  val markovicPayload = MarkovicPayload(corpus)
  log.warning(s"Sending markovicCorpusAPI  the corpus ${markovicPayload}")
  pipeline(Post(markovicCorpusAPI, Some(markovicPayload)))
      
  // now, get a generated sentence, and post it to the WEB!
  // TODO seed this with something else one day...
  val markovicGeneratorAPI = System.getenv("MARKOVIC") + "sentence/I/don%E2%80%99t"
  pipeline(Get(markovicGeneratorAPI)) pipeTo self
  
  val Re = "^([\\d]+)(.*)".r
  
  def receive: Receive = {
    case response: HttpResponse =>
      val body = response.entity.asString
      
      log.error("The publisher will publish this: " + body)
      
      val (num, subject) = website.name match {
        case Re(num, rest) => (num, (num.toInt + 1) + " " + rest)
        case _ => (-1, "1 Very Good Reason This Post Might Suck")
      }
      val blurb = if (num == -1) {
        "We got the counts wrong. But here's some rubbish anyway:"
      } else {
        s"Well, previously it was ${num}, but we added this gem:"
      }
      val newBody = s"""
${website.url}

$blurb
${body}
"""
      
      pipeline(Post(System.getenv("PUBLISHER_API"), FormData(Map("subject" -> subject, "body" -> newBody))))
      
      context.stop(self)
    case x =>
      log.error("Unknown message {}", x)
  }
}
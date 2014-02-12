package actors

import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import models.Get
import akka.actor.ActorLogging

class SourceActor extends Actor with ActorLogging {
  
  import Receptionist._

  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  
  context.setReceiveTimeout(10.seconds)
  
  def receive = {
    case get @ Get(id, link) =>
      log.info("Processing {}", link)
      receptionist ! get
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Results for '$url':\n", "\n", "\n"))
    case Failed(url) =>
      log.error(s"Failed to fetch '$url'\n")
    case ReceiveTimeout =>
      context.stop(self)
  }
}
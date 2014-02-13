package actors

import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import models.{Get, Id, Website}
import akka.actor.ActorLogging

object SourceActor {
  // TODO tweak these based on metrics
  val Delay = 60
  val Scheduler = false
  
  case object ProcessDatabase
}

class SourceActor extends Actor with ActorLogging {
  
  import Receptionist._
  import SourceActor._
  
  import context.dispatcher

  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  
  context.setReceiveTimeout(10.seconds)
  
  
  // let's get started...
  if (Scheduler) {
    context.system.scheduler.scheduleOnce(Delay seconds, self, ProcessDatabase)
  }
  
  def receive = {
    case get @ Get(id, link) =>
      log.info("Processing {}", link)
      receptionist ! get
    case Result(url, set) =>
      log.debug(set.toVector.sorted.mkString(s"RESULTS for '$url':\n", "\n", "\n"))
    case Failed(url) =>
      log.error(s"Failed to fetch '$url'\n")
    case ReceiveTimeout =>
      context.stop(self)
    case ProcessDatabase =>
      val id = Id()
      val website = models.dao.getNextUnprocessed
      log.warning("Processing website {}", website)
      receptionist ! Get(id, website)
      sender ! id
    case x => log.error("Unknown message {}", x)
  }
}
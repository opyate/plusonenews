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
  
  import ScrapeReceiver._
  import SourceActor._
  
  import context.dispatcher

  val receiver = context.actorOf(Props[ScrapeReceiver], "receiver")
  
  // let's get started...
  if (Scheduler) {
    context.system.scheduler.scheduleOnce(Delay seconds, self, ProcessDatabase)
  }
  
  def receive = {
    case get @ Get(id, website) =>
      log.info("Processing {}", website)
      receiver ! get
    case Result(website, corpus) =>
      log.info(s"RESULT for ${website.url} contains ${corpus.size} characters")
    case Failed(website) =>
      log.error(s"Failed to fetch '${website.url}'\n")
    case ProcessDatabase =>
      val id = Id()
      val website = models.dao.getNextUnprocessed
      //receptionist ! Get(id, website)
      self ! Get(id, website)
      sender ! id
    case x => log.error("Unknown message {}", x)
  }
}
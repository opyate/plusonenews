package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.SupervisorStrategy
import akka.actor.ActorLogging
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import akka.actor.ActorRef
import models._

object Controller {
  case class Check(website: Website, depth: Int)
  case class Result(websites: Set[Website])
}

class Controller extends Actor with ActorLogging {
  import Controller._
  
  var cache = Set.empty[Website]
  var children = Set.empty[ActorRef]
  
  context.setReceiveTimeout(10.seconds)
  
  def getterProps(website: Website, depth: Int): Props = Props(new Getter(website, depth))
  
  def receive = {
    case Check(website, depth) =>
      log.debug("{} checking {}", depth, website.url)
      if (!cache(website) && depth > 0)
        children += context.actorOf(getterProps(website, depth - 1))
      cache += website
    case Getter.Done =>
      children -= sender
      if (children.isEmpty)
        context.parent ! Result(cache)
    case ReceiveTimeout =>
      context.children foreach (_ ! Getter.Abort)
  }
  
}
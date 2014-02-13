package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import models._
import akka.actor.ActorLogging

object ScrapeReceiver {
  // TODO set based on certain metrics
  val JobLimit = 3
  
  private case class Job(client: ActorRef, website: Website)
  case class Result(website: Website, corpus: String)
  case class Failed(website: Website)
}

class ScrapeReceiver extends Actor with ActorLogging {
  import ScrapeReceiver._
  
  def scraperProps(website: Website): Props = Props(new Scraper(website))

  var reqNo = 0
  
  var children = Set.empty[ActorRef]

  def receive = waiting

  val waiting: Receive = {
    case get @ Get(id, website) =>
      log.debug("ScrapeReceiver received {}", get)
      context.become(runNext(Vector(Job(sender, website))))
  }

  def running(queue: Vector[Job]): Receive = {
    case Scraper.Result(website, corpus) =>
      log.info(s"ScraperReceiver received corpus with ${corpus.size} characters")
      val job = queue.head
      log.info(s"Sending result to ${job.client.path.name}")
      job.client ! Result(job.website, corpus)
      
      // let the child go
      log.info(s"Children before: ${children.size}")
      children -= sender
      log.info(s"Children after: ${children.size}")
      if (children.isEmpty) {
        log.debug("All Children done")
      }

      context.become(runNext(queue.tail))
    case Scraper.Abort =>
      val job = queue.head
      job.client ! Failed
      
      // let the child go
      children -= sender
      if (children.isEmpty) {
        log.debug("All Children done")
      }
      
      context.become(runNext(queue.tail))
    case Get(id, website) =>
      context.become(enqueueJob(queue, Job(sender, website)))
  }

  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if (queue.isEmpty) waiting
    else {
      children +=  context.actorOf(scraperProps(queue.head.website), s"scraper-$reqNo")
      
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
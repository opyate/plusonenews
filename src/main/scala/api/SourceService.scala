package api

import scala.concurrent.ExecutionContext
import spray.routing.Directives
import spray.http.MediaTypes.{ `application/json` }
import akka.actor.ActorRef
import akka.util.Timeout
import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._
import models._
import models.JsonProtocol._
import akka.actor.ActorLogging
import spray.util.SprayActorLogging
import scala.slick.util.Logging

class SourceService(source: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with SprayJsonSupport with Logging {

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  import actors.SourceActor._
  
  val route = {
    pathPrefix("api") {
      // this app will normally get its links from the linkbaiter database (populated by the linkbaiter scrapy Python app)
      path("link") {
        get {
          // TODO link should have a title with a number in it here, if you're going to +1 anything...
          parameters(
            'url
            )
            .as(Link) { link =>
              respondWithMediaType(`application/json`) {
                complete {
                  models.dao.persistLink(link)
                }
              }
            }
        }
      } ~ path("debug-next") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              (source ? ProcessDatabase).mapTo[Id]
            }
          }
        }
      } ~ path("debug-scrape" / Segment) { scrapeId =>
        get {
          respondWithMediaType(`application/json`) {
            complete {
              models.dao.getScrape(scrapeId)
            }
          }
        }
      }
    }
  }
}

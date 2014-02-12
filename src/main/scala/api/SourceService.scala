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

class SourceService(source: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with SprayJsonSupport {

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  
  val route = {
    pathPrefix("api") {
      path("link") {
        get {
          parameters(
            'url
            )
            .as(Link) { link =>
              respondWithMediaType(`application/json`) {
                complete {
                  val id = Id()
                  source ! Get(id, link)
                  id
                }
              }
            }
        }
      }
    }
  }
}

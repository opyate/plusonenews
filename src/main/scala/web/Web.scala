package web

import akka.actor.actorRef2Scala
import akka.io.IO
import api.Api
import core.Core
import spray.can.Http
import core.CorePlumbing

trait Web {
  this: Api with CorePlumbing with Core =>

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", port = System.getenv("PORT").toInt)

}

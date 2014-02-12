package api

import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.RoundRobinRouter
import core.Core
import core.CorePlumbing
import spray.routing.RouteConcatenation

/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api extends RouteConcatenation {
  this: CorePlumbing with Core =>

  private implicit val _ = system.dispatcher

  val routes =
    new SourceService(source).route

    val rootService: ActorRef =
        system.actorOf(Props(new RoutedHttpService(routes)).withRouter(RoundRobinRouter(nrOfInstances = 10)))

}

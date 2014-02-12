package models

import java.util.UUID
import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._

case class Link(url: String)
case class Id(id: String)
object Id {
  def apply(): Id = Id(UUID.randomUUID().toString())
}
case class Get(id: Id, url: Link)


object JsonProtocol extends DefaultJsonProtocol {
  implicit val linkFormat = jsonFormat1(Link.apply)
  implicit val idFormat = jsonFormat1(Id.apply)
}
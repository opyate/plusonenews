package actors

import spray.json.DefaultJsonProtocol

case class MarkovicPayload(datum: String)

object JsonProtocol extends DefaultJsonProtocol {
  
  implicit val markovicFormat = jsonFormat1(MarkovicPayload.apply)

} 
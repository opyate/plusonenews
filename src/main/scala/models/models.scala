package models

import java.util.UUID
import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import org.joda.time.DateTime
import spray.json.JsValue
import spray.json.RootJsonFormat
import spray.json.JsNumber

case class Link(url: String)
case class Id(id: String)
object Id {
  def apply(): Id = Id(UUID.randomUUID().toString().replaceAll("-", ""))
}
case class Get(id: Id, website: Website)


object JsonProtocol extends DefaultJsonProtocol {
  
  // joda
  implicit object ColorJsonFormat extends RootJsonFormat[DateTime] {
    def write(c: DateTime) = JsNumber(c.getMillis())

    def read(value: JsValue) = value match {
      case JsNumber(value) =>
        new DateTime(value.toLong)
      case _ => spray.json.deserializationError("DateTime expected")
    }
  }
  
  implicit val linkFormat = jsonFormat1(Link.apply)
  implicit val idFormat = jsonFormat1(Id.apply)
  implicit val websiteFormat = jsonFormat7(Website.apply)
  implicit val scrapeFormat = jsonFormat5(Scrape.apply)
}

// database

case class Website(guid: String, name: String, description: String, url: String, status: Int, updated: DateTime, created: DateTime)

// extending, because we're adding apply
object Website extends ((String, String, String, String, Int, DateTime, DateTime) => Website) {
  // TODO convert to 'apply' again, but I can't be arsed to fix the Slick projections when overriding the default 'apply'
  def apply(link: Link): Website = {
    val now = DateTime.now
    Website(Util.md5hex(link.url), "via-api", "via-api", link.url, 0, now, now)
  }
}

import java.security.MessageDigest

object Util {
  val instance = MessageDigest.getInstance("MD5")
  private [this] def md5(s: String) = instance.digest(s.getBytes) 
  def md5hex(s: String) = {
    md5(s).map("%02x".format(_)).mkString
  }
}

case class Scrape(guid: String, websiteGuid: String, scrape: String, updated: DateTime, created: DateTime)
object Scrape extends ((String, String, String, DateTime, DateTime) => Scrape) {
  def apply(text: String, id: Id, website: Website): Scrape = {
    val now = DateTime.now
    Scrape(id.id, website.guid, text, now, now)
  }
}
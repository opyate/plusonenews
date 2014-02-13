package models

import org.joda.time.DateTime
import scala.slick.driver.MySQLDriver.simple._

import com.github.tototoshi.slick.MySQLJodaSupport._

// Definition of the table
class Websites(tag: Tag) extends Table[Website](tag, "website") {
  def guid = column[String]("guid", O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def url = column[String]("url")
  def status = column[Int]("status")
  def updated = column[DateTime]("updated")
  def created = column[DateTime]("created")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (guid, name, description, url, status, updated, created) <> (Website.tupled, Website.unapply)
}

object dao {
  
  // 
  val websites = TableQuery[Websites]

  val dbUser = System.getenv("MYSQL_USER")
  val dbPassword = System.getenv("MYSQL_PASSWD")
  val dbHost = System.getenv("MYSQL_HOST")
  val dbName = System.getenv("MYSQL_DBNAME")
  val dbPort = System.getenv("MYSQL_PORT")
  val db: Database = Database.forURL(s"jdbc:mysql://$dbHost:$dbPort/$dbName?user=$dbUser&password=$dbPassword", driver = "com.mysql.jdbc.Driver")
  
  def getNextUnprocessed(): Website = {
    val now = DateTime.now
    db withTransaction { implicit session: Session =>
      val website = websites.filter(_.status === 0).take(1).first
      
      // update status to show it's being processed
      // TODO can't we update the website instance we just queried?
      val q = for { c <- websites if c.guid === website.guid } yield (c.status, c.updated)
      q.update((1, now))
      
      // return the website
      website.copy(status = 1, updated = now)
    }
  }
  
  def persistLink(link: Link): Website = {
    val now = DateTime.now
    db withTransaction { implicit session: Session =>
      val website = Website(link)
      websites += website
      website
    }
  }
  
}





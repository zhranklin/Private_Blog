package com.zhranklin.homepage.scalikejdbc

import com.typesafe.config.ConfigFactory
import scalikejdbc._
object SqlTest {
  val conf = ConfigFactory.load().getConfig("settings.sql.test")
  val driver = conf.getString("driver")
  val url = conf.getString("url")
  val user = conf.getString("user")
  val pass = conf.getString("password")
  Class.forName(driver)
  ConnectionPool.singleton(url, user, pass)
  implicit val session = AutoSession

  sql"""
  CREATE TABLE members (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64),
    created_at TIMESTAMP NOT NULL
    )
     """.execute.apply()
  Seq("Alice", "Bob", "Chris") foreach { name ⇒
    sql"INSERT INTO members (name, created_at) VALUES ($name, CURRENT_TIMESTAMP)".update.apply()
  }

  val entities = sql"SELECT * FROM members".map(_.toMap).list.apply()

  import org.joda.time._
  case class Member(id: Long, name: Option[String], createdAt: DateTime)
  object Member extends SQLSyntaxSupport[Member] {
    override val tableName = "members"
    def apply(rs: WrappedResultSet): Member = new Member(
      rs.long("id"), rs.stringOpt("name"), rs.jodaDateTime("created_at"))
  }

  val members: List[Member] = sql"SELECT * FROM members".map(Member.apply).list.apply()

  members foreach println

}

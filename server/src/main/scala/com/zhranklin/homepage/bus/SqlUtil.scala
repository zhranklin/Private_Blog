package com.zhranklin.homepage.bus

import com.zhranklin.homepage.bus.Models._
import org.joda.time._
import scalikejdbc._

import scala.collection.mutable

object AppointmentDao {
  import SqlSession.session

  def forDay(date: LocalDate): List[Appointment] =
    forTimeRange(date.toLocalDateTime(LocalTime.MIDNIGHT), date.toLocalDateTime(LocalTime.MIDNIGHT).plusDays(1))

  def forTimeRange(from: LocalDateTime, to: LocalDateTime): List[Appointment] =
    sql"""
         SELECT id FROM appointment
         WHERE app_time BETWEEN ${from} AND ${to}
         ORDER BY app_time, route_id
       """
      .map(_.int("id")).list.apply()
      .flatMap(forId)

  def doAppoint(time: LocalDateTime, routeId: Int, user: User) = {
    DB localTx { implicit session ⇒
      val appoint_idOpt =
        sql"SELECT * FROM appointment WHERE app_time = ${time} AND route_id = ${routeId}"
          .map(_.int("id")).single().apply()
//      val appoint_id: Long = appoint_idOpt.getOrElse(
//        sql"INSERT INTO appointment(app_time, route_id) VALUES(${time}, ${routeId})"
//          .updateAndReturnGeneratedKey().apply())
val appoint_id: Long = 1
      sql"""
         INSERT INTO user_appoint(cust_id, appoint_id)
         VALUES (${user.id}, ${appoint_id})
       """.execute().apply()
    }
  }

  def forId(id: Int): Option[Appointment] =
    sql"""
      SELECT ap.*, COUNT(*) cnt
      FROM appointment ap, user_appoint ua
      WHERE ap.id=${id} AND ua.appoint_id=${id}
      GROUP BY ap.id"""
      .map(rs ⇒ Appointment(
        rs.int("id"),
        rs.jodaLocalDateTime("app_time"),
        RouteDao.forId(rs.int("route_id")).get,
        rs.int("id"),
        rs.intOpt("arrangement_id").flatMap(ArrangementDao.forId)))
      .single().apply()
}

object UserDao {
  import SqlSession.session

  def login(stuId: String, pass: String ⇒ Boolean): Option[User] =
    sql"""SELECT * FROM user WHERE stuId = ${stuId}"""
      .map(mapUserpass)
      .single()
      .apply()
      .filter(u ⇒ pass(u._2))
      .map(_._1)

  def mapUser(rs: WrappedResultSet): User = User(
    rs.int("id"), rs.string("stuId"), rs.string("name"), rs.string("gender"), rs.string("phone"))

  def mapUserpass(rs: WrappedResultSet): (User, String) = (mapUser(rs), rs.string("password"))
}

object RouteDao {
  import SqlSession.session

  def forId(id: Int): Option[Route] =
    sql"""SELECT * FROM route WHERE id = ${id}"""
      .map(mapRoute)
      .single().apply()

  def all =
    sql"""SELECT * FROM route""".map(mapRoute).list().apply()

  def mapRoute(rs: WrappedResultSet): Route = Route(
    rs.int("id"), rs.string("from"), rs.string("to"))
}

object ArrangementDao {
  import SqlSession.session

  val MIN_COUNT = 20

  def aggregatedAppointments(from: LocalDateTime, to: LocalDateTime): (List[Appointment], mutable.Map[(Route, LocalDateTime), LocalDateTime]) = {
    val rawAppoints = AppointmentDao.forTimeRange(from.minusMillis(1), to.minusMillis(1))
    val timeMap = collection.mutable.Map[(Route, LocalDateTime), LocalDateTime]()
    val appoints = rawAppoints
      .groupBy(_.route)
      .flatMap {
        case (route, unsortedAppoints) ⇒
          val appoints = unsortedAppoints.sorted.reverse
          (List.empty[Appointment] /: appoints) {
            case (head :: rest, appoint) if head.count < MIN_COUNT ⇒
              timeMap.put((route, head.app_time), appoint.app_time)
              appoint.plusCount(head.count) :: rest
            case (list, appoint) ⇒ appoint :: list
          }
      }.toList
    (appoints, timeMap)
  }

  def doArrange(from: LocalDateTime, to: LocalDateTime, aggregated: List[Appointment]) = {
    DB localTx { implicit session ⇒
      val arrangements = aggregated.map{ app ⇒
        Arrangement(0, app.app_time, app.route)
      }
      arrangements.foreach { arrange ⇒
        add(arrange)
      }
    }
  }

  def add(arrange: Arrangement) =
    sql"""
         INSERT INTO arrangement(time, route_id)
         SELECT ${arrange.time}, ${arrange.route.id} FROM DUAL
         WHERE NOT EXISTS(SELECT * FROM arrangement WHERE time=${arrange.time} AND route_id=${arrange.route.id})
      """.execute().apply()

  def forId(id: Int): Option[Arrangement] =
    sql"""SELECT * FROM arrangement WHERE id=${id}"""
      .map(rs ⇒ Arrangement(rs.int("id"), rs.jodaLocalDateTime("time"), RouteDao.forId(rs.int("route_id")).get))
      .single().apply()
}
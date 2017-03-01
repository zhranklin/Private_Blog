package com.zhranklin.homepage.bus

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.Credentials
import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.bus.Models._
import org.joda.time._

import scala.concurrent.Future
import scala.util.Try

trait BusRoute extends RouteService {
  def busAuthenticator(credentials: Credentials): Option[User] = credentials match {
    case p@Credentials.Provided(username) ⇒ UserDao.login(username, p.verify)
    case Credentials.Missing ⇒ None
  }

  BusDBInitializer.run()

  val timeTable: Set[LocalTime] = {
    import Stream._
    val start = new LocalTime(8, 0, 0)
    val end = new LocalTime(22, 0, 0)
    def timeStream(from: LocalTime): Stream[LocalTime] = from #:: timeStream(from.plusMinutes(30))
    timeStream(start).takeWhile(_.isBefore(end.plusMillis(1))).toSet
  }

  def dateTimeTable(date: LocalDate): Set[LocalDateTime] = timeTable.map(date.toLocalDateTime)

  import scala.concurrent.ExecutionContext.Implicits._
  Future {
    while(true) {
      println("hello")
      Thread.sleep(10 * 1000)
    }
  }

  abstract override def myRoute = super.myRoute ~
    (pathPrefix("bus") & authenticateBasic("bus security", busAuthenticator)) { user ⇒
      (pathEnd & parameter('date.?, 'route.as[Int].?)) { (dateOpt, routeOpt) ⇒
        val date = dateOpt.flatMap(d ⇒ Try(LocalDate.parse(d)).toOption).getOrElse(LocalDate.now())
        val routes = routeOpt.filter(_ != -1).map(r ⇒ RouteDao.forId(r).toList).getOrElse(RouteDao.all)

        val presetAppoints: Map[(LocalDateTime, Route), Int] =
          dateTimeTable(date)
            .flatMap { datetime ⇒
              routes.map(r ⇒ (datetime, r))
            }
          .map(tp ⇒ (tp, 0)).toMap

        val appoints = presetAppoints
          .++(AppointmentDao.forDay(date).filter(a ⇒ routes.contains(a.route)).map(_.asEntry))
          .toList.map(Appointment.fromEntry).sorted

        println(appoints)
        complete(html.bus_appointment_list.render("今天的预约", appoints))
      } ~
      (post & pathPrefix("appoint") & formField('time, 'route.as[Int])) { (timeStr, routeId) ⇒
        val rawTime = LocalDateTime.parse(timeStr).plusSeconds(150)
        val (date, time) = (rawTime.toLocalDate, rawTime.toLocalTime)
        val actualTime = date.toLocalDateTime(new LocalTime(time.getHourOfDay, time.getMinuteOfHour / 5 * 5))

        AppointmentDao.doAppoint(actualTime, routeId, user)
        redirect("/bus", StatusCodes.SeeOther)
      } ~
      (pathPrefix("arrange") & parameter('from, 'to)) { (fromStr, toStr) ⇒
        val from = LocalDateTime.parse(fromStr)
        val to = LocalDateTime.parse(toStr)
        val (appoints, timeMap) = ArrangementDao.aggregatedAppointments(from, to)
        complete(html.bus_appointment_list.render("本次排班", appoints))
      }
    }
}

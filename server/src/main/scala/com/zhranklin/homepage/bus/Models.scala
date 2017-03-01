package com.zhranklin.homepage.bus

import org.joda.time.LocalDateTime

object Models {
  case class User(id: Int, stuId: String, name: String, gender: String, phone: String)
  case class Appointment(id: Int, app_time: LocalDateTime, route: Route, count: Int, arrange: Option[Arrangement]) extends Ordered[Appointment] {
    def compare(that: Appointment) =
      if (this == that) 0
      else if (this.app_time == that.app_time) this.toString.compareTo(that.toString)
      else if (this.app_time isAfter that.app_time) 1
      else -1
    def asEntry = ((app_time, route), count)
    def plusCount(c: Int) = Appointment(id, app_time, route, count + c, arrange)
  }
  case class Route(id: Int, from: String, to: String)
  case class Bus(busType: String, capacity: Int)
  case class Arrangement(id: Int, time: LocalDateTime, route: Route)
  object Appointment {
    def fromEntry(tp: ((LocalDateTime, Route), Int)) = Appointment(0, tp._1._1, tp._1._2, tp._2, None)
  }
}

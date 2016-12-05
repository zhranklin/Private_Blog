package com.zhranklin.homepage.im

import com.zhranklin.homepage.RouteService

/**
 * Created by Zhranklin on 16/12/5.
 */
trait ImRoute extends RouteService {
  val ips = collection.mutable.Map.empty[String, String]
  abstract override def myRoute = super.myRoute ~
  pathPrefix("im") {
    (pathPrefix("heartbeat" / Segment) & extractClientIP) { (username, ip) ⇒
      ips += (username → ip.toString)
      complete("success")
    } ~ pathPrefix("ips") {
      complete(ips map (tp ⇒ s"${tp._1},${tp._2}") mkString ("", "\n", "\n"))
    }
  }
}

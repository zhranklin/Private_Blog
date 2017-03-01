package com.zhranklin.homepage.im

import com.zhranklin.homepage.RouteService

trait ImRoute extends RouteService {
  val ips = collection.mutable.Map.empty[String, String]
  abstract override def myRoute = super.myRoute ~
  pathPrefix("im") {
    (pathPrefix("heartbeat" / Segment) & extractClientIP) { (username, ip) ⇒ cxt ⇒
      ips += (username → s"${ip.getAddress().get().getHostAddress}:${ip.getPort()}")
      cxt.complete("success\n")
    } ~
    pathPrefix("ips") {
      complete(ips map (tp ⇒ s"${tp._1},${tp._2}") mkString ("", "\n", "\n"))
    }
  }
}

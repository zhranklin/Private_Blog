package com.zhranklin.homepage

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.zhranklin.homepage.blog.BlogRoute
import com.zhranklin.homepage.solr.SolrRoute
import spray.can.Http

import scala.concurrent.duration._

class MyServiceActor extends Actor with BlogRoute with SolrRoute {
  def actorRefFactory = context

  val myRoute = getFromResourceDirectory("") ~ blogRoute ~ solrRoute
  def receive = runRoute(myRoute)
}

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}

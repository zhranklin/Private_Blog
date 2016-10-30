package com.zhranklin.homepage

import akka.http.scaladsl.Http
import akka.util.Timeout

import scala.concurrent.duration._
import scala.io.StdIn

object Boot extends App with MyRouteService {

  import ActorImplicits._

  implicit val timeout = Timeout(5.seconds)

  val bindingFuture = Http().bindAndHandle(myRoute, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
}

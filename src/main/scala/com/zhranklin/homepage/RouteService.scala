package com.zhranklin.homepage

import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.zhranklin.homepage.blog.BlogRoute
import com.zhranklin.homepage.imhere.IMhereRoute
import com.zhranklin.homepage.notice.NoticeRoute
import com.zhranklin.homepage.solr.SolrRoute

trait RouteService extends MyHttpService {
  def myRoute: Route
}

trait BaseRoute extends RouteService {
  def myRoute = getFromResourceDirectory("")
}

object ActorImplicits {

  implicit val system = ActorSystem("on-spray-can")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

}

trait MyRouteService extends BaseRoute with BlogRoute with SolrRoute with NoticeRoute with IMhereRoute

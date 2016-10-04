package com.zhranklin.homepage

import com.zhranklin.homepage.blog.BlogRoute
import com.zhranklin.homepage.imhere.IMhereRoute
import com.zhranklin.homepage.notice.NoticeRoute
import com.zhranklin.homepage.solr.SolrRoute
import spray.routing.Route

trait RouteService extends MyHttpService {
  def myRoute: Route
}

trait BaseRoute extends RouteService {
  def myRoute = getFromResourceDirectory("")
}

trait MyRouteService extends BaseRoute with BlogRoute with SolrRoute with NoticeRoute with IMhereRoute

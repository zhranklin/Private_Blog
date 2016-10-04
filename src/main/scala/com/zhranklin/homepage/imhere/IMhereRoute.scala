package com.zhranklin.homepage.imhere

import com.zhranklin.homepage.RouteService

trait IMhereRoute extends RouteService {
  import com.zhranklin.homepage.JsonSupport._
  abstract override def myRoute = super.myRoute ~
  pathPrefix("imh") {
    pathPrefix("test" / IntNumber) { i ⇒
      val responses = Array(
        IMhereItem("title0", "text", "kkk"),
        IMhereItem("title1", "html", "<h1>head</h1><p>ttext</p>"),
        IMhereItem("title2", "html", "<h2>head2</h2><p>text</p>"),
        IMhereItem("title3", "url", "http://www.baidu.com"),
        IMhereItem("title4", "text", "kkk4"),
        IMhereItem("title5", "text", "kkk5"),
        IMhereItem("title6", "text", "kkk6"),
        IMhereItem("title7", "text", "kkk7"),
        IMhereItem("title8", "text", "kkk8"),
        IMhereItem("title9", "text", "kkk9"))
      complete {
        responses(i)
      }
    }
  }
}


case class IMhereItem(title: String, `type`: String, content: String)

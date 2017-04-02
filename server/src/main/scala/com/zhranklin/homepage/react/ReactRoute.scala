package com.zhranklin.homepage.react

import com.zhranklin.homepage.RouteService

/**
 * Created by Zhranklin on 2017/3/1.
 */
trait ReactRoute extends RouteService {

  abstract override def myRoute = super.myRoute ~
    pathPrefix("react") {
      complete(react.html.index.render("Zhranklin's Blog"))
    } ~
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    }

}

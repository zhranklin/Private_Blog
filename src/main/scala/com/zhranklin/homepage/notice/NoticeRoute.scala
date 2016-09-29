package com.zhranklin.homepage.notice
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import spray.httpx.PlayTwirlSupport._
import spray.routing.HttpService

trait NoticeRoute extends HttpService {

  val news = NoticeServiceObjects.serviceList.par.map(s ⇒ (s.source, s.notices().map(s.toArticle).take(100).par.toList)).toList.toMap

  val noticeRoute =
    path("notice") {
      val sources = news.keys.toList.sorted.map(s ⇒ (s, "/notice/" + encode(s, "utf-8")))
      complete {
        html.notice.render(sources)
      }
    } ~
    path("notice" / Rest) { sourceRaw ⇒
      parameter('url) { url ⇒
        val source = decode(sourceRaw, "utf-8")
        complete {
          html.noticeArticle.render(news(source).filter(a ⇒ decode(a.itemLink, "utf-8").contains(url)).head)
        }
      } ~
      pathEnd {
        val source = decode(sourceRaw, "utf-8")
        news.get(source).map(notices ⇒ complete {
          html.index.render(source, "notice", notices)
        }).get
      }
    }
}

package com.zhranklin.homepage.notice

import com.zhranklin.homepage.{MyHttpService, RouteService}

trait NoticeRoute extends RouteService {
//  NoticeServiceObjects.serviceList.map(_.getUrls.take(3) mkString "\n").foreach(println)
  lazy val news = NoticeServiceObjects.serviceList.map(s ⇒ (s.source, s.notices().map(s.toArticle).take(5).toList)).toList.toMap
  abstract override def myRoute = super.myRoute ~
    path("notice") {
      lazy val sources = news.keys.toList.sorted.map(s ⇒ (s, "/notice/" + encode(s)))
      complete {
        html.notice.render(sources)
      }
    } ~
    path("notice" / Rest) { sourceRaw ⇒
      parameter('url) { url ⇒
        val source = decode(sourceRaw)
        complete {
          html.noticeArticle.render(news(source).filter(a ⇒ decode(a.itemLink).contains(url)).head)
        }
      } ~
      pathEnd {
        val source = decode(sourceRaw)
        news.get(source).map(notices ⇒ complete {
          html.index.render(source, "notice", notices)
        }).get
      }
    }
}

package com.zhranklin.homepage.notice
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import com.zhranklin.homepage.blog.Article
import org.jsoup._
import spray.httpx.PlayTwirlSupport._
import spray.routing.HttpService

import scala.collection.JavaConverters._

object SEtest {
  def getDocUrls(main: String) = Jsoup
    .connect(main)
    .get()
    .select("a[href~=/cs/xsky/xskb/.*]")
    .asScala
    .map(_.attr("abs:href"))
    .filterNot(_.contains("index"))

  def getDoc(url: String) = {
    val doc = Jsoup.connect(url).get()
    val title = doc.head().select("title").text()
    val content = doc.body().select("#BodyLabel").html()
    (title, url, content)
  }

  def toItem(doc: (String, String, String)) = {
    val (title, url, content) = doc
    new NewsItem(title, content, url)
  }
}

class NewsItem(title: String, content: String, val url: String)
  extends Article(title = title, abs = "", author = "scu_cs", html = content) {
  override val itemLink = "/setest?url=" + encode(url, "utf-8")
}

trait NoticeRoute extends HttpService {
  import SEtest._

  val news_pieces =
    getDocUrls("http://cs.scu.edu.cn/cs/index.htm")
      .map(getDoc)
      .map(toItem)

  val news = NoticeServiceObjects.serviceList.par.map(s ⇒ (s.source, s.notices().map(s.toArticle).take(100).par.toList)).toList.toMap

  val noticeRoute =
    path("notice") {
      val sources = news.keys.toList.sorted.map(s ⇒ (s, "/notice/" + encode(s, "utf-8")))
      complete {
        html.notice.render(sources)
      }
    } ~
    path("notice" / Rest) { sourceRaw ⇒
      parameter('url) { rurl ⇒
        val source = decode(sourceRaw, "utf-8")
        complete {
          val url = java.net.URLDecoder.decode(rurl, "utf-8")
          println(url)
          println(source)
          println(rurl)
          println(sourceRaw)
          println(news.get(source).get.map(_.itemLink) mkString "\nxxx")
          html.noticeArticle.render(news.get(source).get.filter(a ⇒ decode(a.itemLink, "utf-8").contains(rurl)).head)
        }
      } ~
      pathEnd {
        val source = decode(sourceRaw, "utf-8")
        println(source)
        news.get(source).map(notices ⇒ complete {
          html.index.render(source, "notice", notices)
        }).get
      }
    }
}

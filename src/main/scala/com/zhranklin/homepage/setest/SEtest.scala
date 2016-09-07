package com.zhranklin.homepage.setest
import java.net.URLEncoder

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
  override val itemLink = "/setest/" + URLEncoder.encode(url, "utf-8")
}

trait SEtestRoute extends HttpService {
  import SEtest._

  val news_pieces =
    getDocUrls("http://cs.scu.edu.cn/cs/index.htm")
      .map(getDoc)
      .map(toItem)

  val seTestRoute =
    path("setest") {
      complete {
        html.index.render("test news", "search", news_pieces)
      }
    } ~
    path("setest" / Rest) { rurl ⇒
      complete {
        val url = java.net.URLDecoder.decode(rurl, "utf-8")
        println(url)
        html.setestarticle.render(news_pieces.filter(_.url == url).head)
      }
    }
}

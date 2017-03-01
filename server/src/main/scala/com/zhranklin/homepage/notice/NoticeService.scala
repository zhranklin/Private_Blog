package com.zhranklin.homepage.notice

import java.net.URLEncoder
import java.util.Date

import com.zhranklin.homepage.blog.Article

import scala.util.Try

trait IndexService {
  protected val template: String
  protected val firstIndex: Int = 1
  protected def valueStream(i: Int): Stream[Int] = i #:: valueStream(i + 1)
  protected def indexNums: Iterable[Any] = valueStream(firstIndex)
  protected def interpolate(value: Any): String = template.replaceAll("<index>", value.toString)
  def indexUrls: Iterable[String] = indexNums map interpolate
}

case class Notice(url: String, title: String, html: String, date: Date)
case class NoticeEntry(url: String, title: Option[String] = None)

abstract class NoticeService(val source: String) extends UrlService with IndexService with NoticeFetcher {
  def getUrls: Iterable[NoticeEntry] = indexUrls.map(u ⇒ Try(noticeUrlsFromUrl(u))).takeWhile(_.isSuccess).flatMap(_.get)
  def notices(after: Date = new Date(0)): Iterable[Notice] = getUrls.map(n ⇒ Try(fetch(n))).filter(_.isSuccess).take(30).map(_.get).takeWhile(_.date.after(after))
  def toArticle(notice: Notice) = new Article(notice.title, source, "notice", None, notice.html, "", Nil, notice.date) {
    override val itemLink = s"/notice/$source?url=" + URLEncoder.encode(notice.url, "utf-8")
  }
}

package com.zhranklin.homepage.notice

import java.net.URLEncoder
import java.util.Date

import com.zhranklin.homepage.blog.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.collection.JavaConverters._
import scala.util.Try

case class Notice(url: String, title: String, html: String, date: Date) {
}

case class NoticeEntry(url: String, title: Option[String] = None)

class NoticeService(val source: String, indexService: IndexService, noticeFetcher: NoticeFetcher) {
  protected def getUrls: Iterable[NoticeEntry] = indexService.indexUrls.map(u ⇒ Try(indexService.noticeUrlsFromUrl(u))).takeWhile(_.isSuccess).flatMap(_.get)

  def notices(after: Date = new Date(0)): Iterable[Notice] = getUrls.map(n ⇒ Try(noticeFetcher.fetch(n)))
      .takeWhile(_.isSuccess).map(_.get).takeWhile(_.date.after(after))

  def toArticle(notice: Notice) = new Article(notice.title, source, None, notice.html, "", Nil, notice.date) {
    override val itemLink = s"/notice/$source?url=" + URLEncoder.encode(notice.url, "utf-8")
  }
}

abstract class IndexService(template: String, firstIndex: Int = 1) {
  private def valueStream(i: Int): Stream[Int] = i #:: valueStream(i + 1)
  protected def indexNums: Iterable[Any] = valueStream(firstIndex)
  protected def interpolate(value: Any): String = template.replaceAll("<index>", value.toString)
  type E
  protected def rawUrlsFromDoc(doc: Document): Iterable[E]
  protected def extractFromRawUrl(a: E): NoticeEntry

  def indexUrls: Iterable[String] = indexNums map interpolate

  def noticeUrlsFromUrl(url: String): Iterable[NoticeEntry] = {
    val doc = Jsoup.connect(url).get
    rawUrlsFromDoc(doc) map extractFromRawUrl
  }
}

class SelectorIndexService(urlPattern: String, template: String, firstIndex: Int = 1)
  extends IndexService(template, firstIndex) {
  type E = Element
  def rawUrlsFromDoc(doc: Document) = doc.select(s"a[href~=$urlPattern]").asScala
  def extractFromRawUrl(a: Element) = NoticeEntry(a.attr("abs:href"), Some(a.text))
}

trait NoticeFetcher {
  protected def parse(doc: Document, title: Option[String]): Notice
  private val splits = List("-", "\\.", "/")
  private val d2 = "\\d{1,2}"
  private val dPattern = String.join("|", splits.map(s ⇒ s"(?:(?:\\d{4}$s)?$d2$s$d2(?:\\s+$d2:$d2(?::$d2)?)?)").asJava)
  private val dMatch = s".*?(?<!\\d)($dPattern)(?!\\d).*?".r
  protected def exDate(arg: String) = {
    val dMatch(dStr) = arg
    new Date(dStr.replaceAll(splits mkString "|", "/"))
  }
  private def absLink(doc: Document) = {
    doc.select("[href]").asScala.map(link ⇒ link.attr("href", link.attr("abs:href")))
    doc.select("[src]").asScala.map(link ⇒ link.attr("src", link.attr("abs:src")))
    doc
  }
  def fetch(entry: NoticeEntry): Notice = parse(absLink(Jsoup.connect(entry.url).get), entry.title)
}

object NoticeFetcher {
  def selectorF(selector: String)(then: Elements ⇒ String) = ((_: Document).select(selector)).andThen(then)
  def dateF(selector: String) = selectorF(selector)(_.first.text)
  def contentF(selector: String) = selectorF(selector)(_.first.html)
}

class FunNoticeFetcher(getContent: Document ⇒ String, getDateStr: Document ⇒ String) extends NoticeFetcher {
  def parse(doc: Document, title: Option[String]) = {
    println(doc.baseUri)
    Notice(doc.baseUri, title.getOrElse(doc.title), getContent(doc), exDate(getDateStr(doc)))
  }
}

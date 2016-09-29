package com.zhranklin.homepage.notice

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import com.zhranklin.homepage.Util._

abstract class UrlService {
  def noticeUrlsFromUrl(url: String): Iterable[NoticeEntry]
}

abstract class AbstractUrlService extends UrlService {
  type E
  protected def rawUrlsFromDoc(doc: Document): Iterable[E]
  protected def extractFromRawUrl(a: E): NoticeEntry
  def noticeUrlsFromUrl(url: String): Iterable[NoticeEntry] = {
    val doc = Jsoup.connect(url).get
    rawUrlsFromDoc(doc) map extractFromRawUrl
  }
}

class SelectorUrlService(urlPattern: String)
  extends AbstractUrlService {
  type E = Element
  def rawUrlsFromDoc(doc: Document) = doc.select(s"a[href~=$urlPattern]").asScala
  def extractFromRawUrl(a: Element) = NoticeEntry(a.attr("abs:href"), Some(a.text))
}


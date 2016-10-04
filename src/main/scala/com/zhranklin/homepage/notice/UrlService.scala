package com.zhranklin.homepage.notice

import com.zhranklin.homepage.JsoupUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import com.zhranklin.homepage.Util._

trait UrlService {
  def noticeUrlsFromUrl(url: String): Iterable[NoticeEntry]
}

trait AbstractUrlService extends UrlService {
  type E
  protected def rawUrlsFromDoc(doc: Document): Iterable[E]
  protected def extractFromRawUrl(a: E): NoticeEntry
  def noticeUrlsFromUrl(url: String): Iterable[NoticeEntry] = {
    val doc = Jsoup.connect(url).get
    rawUrlsFromDoc(doc) map extractFromRawUrl
  }
}

trait SelectorUrlService
  extends AbstractUrlService {
  val urlPattern: String
  type E = Element
  def rawUrlsFromDoc(doc: Document) = doc.select(s"a[href~=$urlPattern]").asScala
  def extractFromRawUrl(a: Element) = NoticeEntry(a.attr("abs:href"), Some(a.text))
}

trait UniversalUrlService extends UrlService with JsoupUtil {

  def noticeUrlsFromUrl(indexUrl: String) = {
    println(s"index: $indexUrl")
    def tryGroup(urls: Seq[Element]) = {
      def properGroup(pre: Int, post: Int) = {
        val counts = groupByPrePostFix(urls, pre, post).map(_._2.size)
        counts.count(_ > 5) - (urls.size - counts.max) / 18
      }
      def pMax(pre: Boolean = true) = 1 to 200 takeWhile { n ⇒ (if(pre) properGroup(n, 0) else properGroup(0, n)) > 0} last
      val preMax = pMax()
      val postMax = pMax(false)
      0
    }
    def groupByPrePostFix(urls: Seq[Element], pre: Int, post: Int) =
      urls.groupBy(e ⇒ (e.href.take(pre), e.href.takeRight(post)))
    println("aaaaaa")
    def longEnough(urls: Seq[Element]) = urls.map(_.text.length).sum / urls.size.asInstanceOf[Double] > 7
    println("aaaaaa")
    val doc = Jsoup.connect(indexUrl).get
    println("aaaaaa")
    doc.body select "*:not(:has(a[href]))" select "*:not(a[href])" remove
    val urls = doc.select("*:last-of-type:nth-of-type(n+5)").asScala.flatMap(_.parent.select("a[href]").asScala)
    println("aaaaaa")
    def LongUrlsWithFun(pre: Int, post: Int)(urls: Seq[Element]) =
      groupByPrePostFix(urls, tryGroup(urls), 0).values.filter(longEnough).flatten.toSeq
    val longUrls = LongUrlsWithFun(0, 0) _ andThen LongUrlsWithFun(0,0)
    try {
      val urlsLongEnough = longUrls(urls)
      println("aaaaaa")
      val anotherTry = tryGroup(urlsLongEnough)
      println("aaaaaa")
      val shrunkPrefixSize = "^.*?(?=\\d*$)".r.findFirstIn(urlsLongEnough.head.href.take(anotherTry)).map(_.length).getOrElse(anotherTry)
      println("aaaaaa")
      val ret = groupByPrePostFix(urlsLongEnough, shrunkPrefixSize, 0).values.filter(_.size > 5).flatten.map(e ⇒ NoticeEntry(e.absHref, Some(e.text)))
      println(s"*****\nret: $ret\n*****")
      ret
    } catch {
      case e: UnsupportedOperationException ⇒ Nil
    }
  }
}

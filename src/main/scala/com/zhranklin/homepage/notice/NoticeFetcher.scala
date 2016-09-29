package com.zhranklin.homepage.notice

import java.util.Date

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.zhranklin.homepage.Util._
import org.jsoup.select.Elements

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
    List("href", "src").foreach(n ⇒ doc.select("[$n]").asScala.map(l ⇒ l.attr(n, l.attr("abs:$n"))))
    doc
  }
  def fetch(entry: NoticeEntry): Notice = parse(absLink(Jsoup.connect(entry.url).get), entry.title)
}

object NoticeFetcher {
  def selectorF(selector: String)(Then: Elements ⇒ String) = ((_: Document).select(selector)).andThen(Then)
  def dateF(selector: String) = selectorF(selector)(_.first.text)
  def contentF(selector: String) = selectorF(selector)(_.first.html)
}

class FunNoticeFetcher(getContent: Document ⇒ String, getDateStr: Document ⇒ String) extends NoticeFetcher {
  def parse(doc: Document, title: Option[String]) = {
    println(doc.baseUri)
    Notice(doc.baseUri, title.getOrElse(doc.title), getContent(doc), exDate(getDateStr(doc)))
  }
}

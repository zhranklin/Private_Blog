package com.zhranklin.homepage.notice

import java.util.Date

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.zhranklin.homepage.Util
import org.jsoup.select.Elements

import scala.util.Try

trait NoticeFetcher extends Util {
  protected def parse(doc: Document, title: Option[String]): Notice
  private val splits = List("-", "\\.", "/")
  private val d2 = "\\d{1,2}"
  private val dPattern = String.join("|", splits.map(s ⇒ s"(?:(?:\\d{4}$s)?$d2$s$d2(?:\\s+$d2:$d2(?::$d2)?)?)").asJava)
  private val dMatch = s".*?(?<!\\d)($dPattern)(?!\\d).*?".r
  protected def exDate(arg: String) = {
    val dMatch(dStr) = arg
    new Date(dStr replaceAll (splits mkString "|", "/"))
  }
  private def absLink(doc: Document) = {
    List("href", "src").foreach(n ⇒ doc.select(s"[$n]").asScala.map(l ⇒ l.attr(n, l.attr(s"abs:$n"))))
    doc
  }
  def fetch(entry: NoticeEntry): Notice = parse(absLink(Jsoup.connect(entry.url).get), entry.title)
}

trait FunNoticeFetcher extends NoticeFetcher {
  def selectorF(selector: String)(Then: Elements ⇒ String) = ((_: Document).select(selector)).andThen(Then)
  def dateF(selector: String) = selectorF(selector)(_.first.text)
  def contentF(selector: String) = selectorF(selector)(_.first.html)
  val getContent: Document ⇒ String
  val getDateStr: Document ⇒ String
  def parse(doc: Document, title: Option[String]) = {
    println(doc.baseUri)
    Notice(doc.baseUri, title.getOrElse(doc.title), getContent(doc), exDate(getDateStr(doc)))
  }
}

trait UniversalNoticeFetcher extends NoticeFetcher { self: UniversalUrlService ⇒
  val splits = List("-", "\\.", "/")
  val d2 = "\\d{1,2}"
  val dPattern = String.join("|", splits.map(s ⇒ s"(?:(?:\\d{4}$s)?$d2$s$d2(?:\\s+$d2:$d2(?::$d2)?)?)").asJava)
  val dMatch = s".*?(?<!\\d)($dPattern)(?!\\d).*?".r
  def disLink(doc: Document) = {
    val urls =  noticeUrlsFromUrl(doc.baseUri).map(_.url)
    urls.foreach { u ⇒
      val sel = s"""a[abs:href="$u"]"""
      val e = doc.select(sel).first
      val text = e.text.replaceAll("\\s+", "")
      var p = e
      while(p.parent.text.replaceAll("\\s+", "") == text) p = p.parent
      p.remove
    }
  }
  def getTime(e: Element): Option[(Date, Int, Element)] = Try(dMatch.unapplySeq(e.text).get.head)
    .map(_.replaceAll(splits mkString "|", "/"))
    .map(new Date(_)).filter(i ⇒ e.text.replaceAll("\\s+", "").length < 60).toOption
    .map {(_, List("更新", "阅读", "来源", "时间", "编辑", "责任").count(e.text.contains)*100 + e.select("*").size, e)}

  def properTime(doc: Document) = doc.select("*").asScala.flatMap(getTime).maxBy(_._2)

  protected def parse(doc: Document, title: Option[String]) = {
    disLink(doc)
    val time = properTime(doc)
    val html = time._3.siblingElements.asScala.maxBy(_.text.length).html
    Notice(doc.baseUri, title.getOrElse(doc.title), html, time._1)
  }
}
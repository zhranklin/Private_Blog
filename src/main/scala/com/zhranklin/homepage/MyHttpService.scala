package com.zhranklin.homepage

import java.net.{URLDecoder, URLEncoder}

import org.jsoup.Jsoup
import spray.httpx.PlayTwirlSupport
import spray.routing.HttpService

import scala.collection.convert.{DecorateAsJava, DecorateAsScala}
import scala.reflect.runtime._

trait Util extends DecorateAsJava with DecorateAsScala {
  val encode = URLEncoder.encode(_: String, "utf-8")
  val decode = URLDecoder.decode(_: String, "utf-8")
}

trait MyHttpService extends Util with PlayTwirlSupport with HttpService

trait HaveOID

object Util extends Util

trait JsoupUtil extends Util {
  type Element = org.jsoup.nodes.Element
  type Document = org.jsoup.nodes.Document
  type Elements = org.jsoup.select.Elements
  implicit class RichElement(element: Element) {
    def href = element.attr("href")
    def absHref = element.attr("abs:href")
  }

  type Date = java.util.Date
  type Try[E] = scala.util.Try[E]
}

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
//  implicit class GetFieldClass(obj: Any) {
//    import scala.reflect.runtime.{universe ⇒ ru}
//    def <>(method: Symbol) = ru.runtimeMirror(obj.getClass.getClassLoader)
//      .reflect(obj).reflectMethod(ru.typeOf[obj.type]
//      .declaration(ru.TermName(method.name)).asMethod)()
//  }
}

trait MyHttpService extends Util with PlayTwirlSupport with HttpService

object Util extends Util

trait JsoupUtil extends Util {
  import scala.reflect.runtime.universe._
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

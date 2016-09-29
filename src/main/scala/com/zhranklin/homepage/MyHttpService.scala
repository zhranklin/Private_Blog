package com.zhranklin.homepage

import java.net.{URLDecoder, URLEncoder}

import org.jsoup.Jsoup
import spray.httpx.PlayTwirlSupport
import spray.routing.HttpService

import scala.collection.convert.{DecorateAsJava, DecorateAsScala}

trait Util extends DecorateAsJava with DecorateAsScala {
  val encode = URLEncoder.encode(_: String, "utf-8")
  val decode = URLDecoder.decode(_: String, "utf-8")
}

trait MyHttpService extends Util with PlayTwirlSupport with HttpService

object Util extends Util

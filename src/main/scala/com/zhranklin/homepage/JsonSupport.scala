package com.zhranklin.homepage

import spray.httpx.Json4sJacksonSupport

trait JsonSupport extends Json4sJacksonSupport {
  import org.json4s.DefaultFormats
  override implicit def json4sJacksonFormats = DefaultFormats
}

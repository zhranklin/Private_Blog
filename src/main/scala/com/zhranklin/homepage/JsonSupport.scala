package com.zhranklin.homepage

import com.zhranklin.homepage.imhere.Model._
import org.bson.types.ObjectId
import org.json4s._
import spray.httpx.Json4sJacksonSupport
import FieldSerializer._

trait JsonSupport extends Json4sJacksonSupport {
  import org.json4s.DefaultFormats
  val OIDSer = new CustomSerializer[ObjectId](formats ⇒ ({
    case JString(str) ⇒ new ObjectId(str)
  }, {
    case id: ObjectId ⇒ JString(id.toString)
  }))

  override implicit def json4sJacksonFormats = DefaultFormats + OIDSer
}

object JsonSupport extends JsonSupport

package com.zhranklin.homepage

import java.net.{URLDecoder, URLEncoder}

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.bson.types.ObjectId
import org.json4s.{CustomSerializer, Formats, jackson, _}
import play.twirl.api.{Html, Txt, Xml}

import scala.collection.convert.{DecorateAsJava, DecorateAsScala}

trait Util extends DecorateAsJava with DecorateAsScala {
  val encode = URLEncoder.encode(_: String, "utf-8")
  val decode = URLDecoder.decode(_: String, "utf-8")
  def getToken = System.getenv("PSW")
}

trait MyHttpService extends Util with PlayTwirlSupport with JsonSupport with Directives

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

trait JsonSupport extends Json4sSupport {

  import org.json4s.DefaultFormats

  val OIDSer = new CustomSerializer[ObjectId](formats ⇒ ( {
    case JString(str) ⇒ new ObjectId(str)
  }, {
    case id: ObjectId ⇒ JString(id.toString)
  }))

  implicit val json4sJacksonFormats: Formats = DefaultFormats + OIDSer
  implicit val serialization = jackson.Serialization
}

trait PlayTwirlSupport {

  implicit val twirlHtmlMarshaller = twirlMarshaller[Html](`text/html`)
  implicit val twirlTxtMarshaller = twirlMarshaller[Txt](`text/plain`)
  implicit val twirlXmlMarshaller = twirlMarshaller[Xml](`text/xml`)

  protected def twirlMarshaller[A <: AnyRef : Manifest](contentType: MediaType): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(contentType)(_.toString)

}

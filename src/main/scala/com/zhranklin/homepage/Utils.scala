package com.zhranklin.homepage

import java.net.{URLDecoder, URLEncoder}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server.Directives
import org.bson.types.ObjectId
import play.twirl.api.{Html, Txt, Xml}
import spray.json.DefaultJsonProtocol

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

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import spray.json._

  implicit object OIDFormat extends RootJsonFormat[ObjectId] {
    def write(id: ObjectId) = JsString(id.toString)
    def read(value: JsValue) = value match {
      case JsString(id) ⇒ new ObjectId(id)
      case _ ⇒ deserializationError("string literal expected")
    }
  }

  import com.zhranklin.homepage.imhere.Model._
  import com.zhranklin.homepage.solr._
  implicit val placeFormat = jsonFormat2(Place)
  implicit val itemFormat = jsonFormat5(Item)
  implicit val sdFormat = jsonFormat(SolrDoc, "tstamp", "title", "url", "content")
  implicit val sqresFormat = jsonFormat1(SolrQueryResponse)
  implicit val sqrusFormat = jsonFormat1(SolrQueryResult)
}

trait PlayTwirlSupport {

  implicit val twirlHtmlMarshaller = twirlMarshaller[Html](`text/html`)
  implicit val twirlTxtMarshaller = twirlMarshaller[Txt](`text/plain`)
  implicit val twirlXmlMarshaller = twirlMarshaller[Xml](`text/xml`)

  protected def twirlMarshaller[A <: AnyRef : Manifest](contentType: MediaType): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(contentType)(_.toString)

}

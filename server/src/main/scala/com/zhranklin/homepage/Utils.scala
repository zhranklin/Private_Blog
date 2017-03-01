package com.zhranklin.homepage

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JObject
import org.json4s._
import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}

import scala.collection.convert.{DecorateAsJava, DecorateAsScala}

trait Util extends DecorateAsJava with DecorateAsScala {
  import java.net.{URLDecoder, URLEncoder}
  val encode = URLEncoder.encode(_: String, "utf-8")
  val decode = URLDecoder.decode(_: String, "utf-8")
  def getToken = System.getenv("PSW")
  trait _idRename
}

trait JsonForMongo {
  import FieldSerializer._

  class OidFormat extends CustomSerializer[ObjectId](format ⇒ ({
    case JObject(JField("$oid", JString(oid)) :: Nil) ⇒ new ObjectId(oid)
  }, {
    case oid: ObjectId ⇒ JObject("$oid" → JString(oid.toHexString))
  }))

  implicit val formats = DefaultFormats +
    FieldSerializer[com.zhranklin.homepage.Util._idRename](renameTo("id", "_id"), renameFrom("_id", "id")) +
    new OidFormat

  implicit val ser = jackson.Serialization

//  def readMongo[T: Manifest](m: DBObject) = ser.read[T](com.mongodb.util.JSON.serialize(m))

//  def writeMongo[T <: AnyRef](obj: T): DBObject = $(ser.write(obj))

  implicit class FromDBObject(m: DBObject) {
    def read[T: Manifest]: T = ser.read[T](com.mongodb.util.JSON.serialize(m))
  }

  implicit class ToDBObject[T <: AnyRef](obj: T) {
    def mongo: DBObject = $(ser.write(obj))
  }

}

object JsonForMongo extends JsonForMongo


import akka.http.scaladsl.server.Directives
trait MyHttpService extends Util with PlayTwirlSupport with Directives

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

//import spray.json.DefaultJsonProtocol
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//trait JsonSupport1 extends SprayJsonSupport with DefaultJsonProtocol {
//  import org.bson.types.ObjectId
//  import spray.json._
//
//  implicit object OIDFormat extends RootJsonFormat[ObjectId] {
//    def write(id: ObjectId) = JsString(id.toString)
//    def read(value: JsValue) = value match {
//      case JsString(id) ⇒ new ObjectId(id)
//      case _ ⇒ deserializationError("string literal expected")
//    }
//  }
//
//  import com.zhranklin.homepage.imhere.Model._
//  import com.zhranklin.homepage.solr._
//  implicit val placeFormat = jsonFormat2(Place)
//  implicit val itemFormat = jsonFormat6(Item)
//  implicit val sdFormat = jsonFormat(SolrDoc, "tstamp", "title", "url", "content")
//  implicit val sqresFormat = jsonFormat1(SolrQueryResponse)
//  implicit val sqrusFormat = jsonFormat1(SolrQueryResult)
//}

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
trait BasicJsonSupport extends Json4sSupport {
  import org.json4s.DefaultFormats
  implicit val serialization = org.json4s.jackson.Serialization
  implicit val formats = DefaultFormats + new OidFormat
  class OidFormat extends CustomSerializer[ObjectId](format ⇒ ({
    case JString(oid) ⇒ new ObjectId(oid)
  }, {
    case oid: ObjectId ⇒ JString(oid.toHexString)
  }))
}

object BasicJsonSupport extends BasicJsonSupport

trait PlayTwirlSupport {
  import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
  import akka.http.scaladsl.model.MediaType
  import akka.http.scaladsl.model.MediaTypes._
  import play.twirl.api.{Html, Txt, Xml}

  implicit val twirlHtmlMarshaller = twirlMarshaller[Html](`text/html`)
  implicit val twirlTxtMarshaller = twirlMarshaller[Txt](`text/plain`)
  implicit val twirlXmlMarshaller = twirlMarshaller[Xml](`text/xml`)

  protected def twirlMarshaller[A <: AnyRef : Manifest](contentType: MediaType): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(contentType)(_.toString)

}

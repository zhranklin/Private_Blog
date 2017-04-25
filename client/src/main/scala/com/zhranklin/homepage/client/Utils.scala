package com.zhranklin.homepage.client

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery
import upickle._
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Created by Zhranklin on 2017/3/31.
 */
object globalObj {
  val JsObj = scala.scalajs.js.Dynamic
}
trait Imports {
  val $ = jQuery
  val JsObj = globalObj.JsObj
  val JS = JsObj.global
  val document = org.scalajs.dom.document
  val window = org.scalajs.dom.window
}

object Imports extends Imports

object MyClient extends autowire.Client[Js.Value, Reader, Writer] {
  override def doCall(req: Request): Future[Js.Value] = {
    Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.json.write(Js.Obj(req.args.toSeq: _*))
    ).map(_.responseText)
      .map(upickle.json.read)
  }

  def read[Result: Reader](p: Js.Value) = readJs[Result](p)
  def write[Result: Writer](r: Result) = writeJs(r)
}

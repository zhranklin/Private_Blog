package com.zhranklin.homepage.client

import org.scalajs.dom.ext.Ajax
import upickle._
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Created by Zhranklin on 2017/3/31.
 */
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

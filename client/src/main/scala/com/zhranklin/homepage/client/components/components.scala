package com.zhranklin.homepage.client

import com.zhranklin.homepage.client.components.Modules
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.{Listenable, OnUnmount}
import japgolly.scalajs.react.vdom.PackageBase
import japgolly.scalajs.react.{Children, CtorType, ReactEventTypes, vdom}

/**
 * Created by zhranklin on 2017/5/10.
 */
package object components extends ComponentUtils
trait ComponentUtils extends PackageBase with ReactEventTypes {
  import vdom.{HtmlAttrAndStyles, HtmlTags}

  implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  val < = HtmlTags
  val ^ = HtmlAttrAndStyles
  val ScalaComponent = japgolly.scalajs.react.ScalaComponent
  type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = ScalaComponent.Component[P, S, B, CT]
  val Callback = japgolly.scalajs.react.Callback
  val CallbackTo = japgolly.scalajs.react.CallbackTo
  type Callback = japgolly.scalajs.react.Callback
  val Page = com.zhranklin.homepage.client.Page
  type Page = com.zhranklin.homepage.client.Page
  val $ = Modules.JQuery
  val JsObj = scala.scalajs.js.Dynamic
  type JsObj = scala.scalajs.js.Dynamic
  val JS = JsObj.global
  val document = org.scalajs.dom.document
  val window = org.scalajs.dom.window

  def handleInput[P, S]($: BackendScope[P, S])(mod: String ⇒ S ⇒ S) =
    (e: ReactEventFromInput) ⇒ Callback(e.persist()) >> $.modState(mod(e.target.value))
  def pipeInput[P, S]($: BackendScope[P, S])(mod: String ⇒ S ⇒ S) =
    (e: ReactEventFromInput) ⇒ Callback(e.persist()) >> $.modState(mod(e.target.value)) >> CallbackTo(e.target.value)
  type StateW[P, S, B] = japgolly.scalajs.react.component.builder.Lifecycle.StateW[P, S, B]

  object Listenable {
    def listen[P, C <: Children, S, B <: OnUnmount, A](listenable: P => Listenable[A],
                                                       makeListener: StateW[P, S, B] => A => Callback): ScalaComponent.Config[P, C, S, B] =
      OnUnmount.install[P, C, S, B] andThen
        (_.componentDidMount($ => listenable($.props).register(makeListener($)) >>= $.backend.onUnmount)) andThen
        (_.componentDidUpdate($ ⇒ listenable($.currentProps).register(makeListener($)) >>= $.backend.onUnmount))

  }

  object DidRender {
    def did_P[P, C <: Children, S, B](f: P ⇒ Callback): ScalaComponent.Config[P, C, S, B] =
      _.componentDidMount($ ⇒ f($.props))
        .componentDidUpdate($ ⇒ f($.currentProps))
  }

  implicit class CallbackOption[T](opt: Option[T]) {
    def foreachCb(cb: T ⇒ Callback) = opt.map(cb).getOrElse(Callback.empty)
  }

}

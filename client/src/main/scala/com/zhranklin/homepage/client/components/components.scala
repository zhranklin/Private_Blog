package com.zhranklin.homepage.client

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.{Listenable, OnUnmount}
import japgolly.scalajs.react.{Children, CtorType, ReactEventTypes, ScalaComponentConfig, vdom}

/**
 * Created by zhranklin on 2017/5/10.
 */
trait ComponentUtils extends vdom.PackageBase with ReactEventTypes {
  implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  import vdom.{HtmlAttrAndStyles, HtmlTags}
  val < = HtmlTags
  val ^ = HtmlAttrAndStyles
  val ScalaComponent = japgolly.scalajs.react.ScalaComponent
  type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = ScalaComponent.Component[P, S, B, CT]
  val Callback = japgolly.scalajs.react.Callback
  type Callback = japgolly.scalajs.react.Callback
  val Page = com.zhranklin.homepage.client.Page
  type Page = com.zhranklin.homepage.client.Page
  val $ = org.scalajs.jquery.jQuery
  val JsObj = scala.scalajs.js.Dynamic
  type JsObj = scala.scalajs.js.Dynamic
  val JS = JsObj.global
  val document = org.scalajs.dom.document
  val window = org.scalajs.dom.window
  def handleInput[P, S]($: BackendScope[P, S])(mod: String ⇒ S ⇒ S) =
    (e: ReactEventFromInput) ⇒ Callback(e.persist()) >> $.modState(mod(e.target.value))
  object Listenable {

    def listen[P, C <: Children, S, B <: OnUnmount, A](listenable: P => Listenable[A],
                                                       makeListener: ScalaComponent.Lifecycle.StateRW[P, S, B] => A => Callback): ScalaComponentConfig[P, C, S, B] =
      OnUnmount.install[P, C, S, B] andThen
        (_.componentDidMount($ => listenable($.props).register(makeListener($)) >>= $.backend.onUnmount)) andThen
        (_.componentWillReceiveProps($ ⇒ listenable($.nextProps).register(makeListener($)) >>= $.backend.onUnmount))

  }
}

package object components extends ComponentUtils

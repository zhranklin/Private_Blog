package com.zhranklin.homepage.client.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.{Broadcaster, OnUnmount}

import scala.concurrent.Future

object AsyncVdom {

  type Props = (VdomElement, Broadcaster[VdomElement])
  type State = VdomElement

  class Backend($: BackendScope[Props, State]) extends OnUnmount

  val comp = ScalaComponent.builder[Props]("AsyncVdom")
    .initialState_P(_._1)
    .backend(new Backend(_))
    .render_S(identity)
    .configure(Listenable.listen(_._2, $ ⇒ $.setState(_: State)))
    .build
  def future(future: Future[VdomElement], default: VdomElement = <.div(<.p("loading..."))) =
    comp(default, new Broadcaster[VdomElement] {
      future.foreach{ dom ⇒
        broadcast(dom).runNow()
      }
    })
}

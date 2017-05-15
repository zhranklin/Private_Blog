package com.zhranklin.homepage.client.components

import com.zhranklin.homepage.Apis.AcmApi
import com.zhranklin.homepage.Dtos.Problem
import com.zhranklin.homepage.client.MyClient
import com.zhranklin.homepage.client.components.MainApp.BC
import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.{Broadcaster, OnUnmount}
import japgolly.scalajs.react.extra.router.RouterCtl

import scala.concurrent.Future

/**
 * Created by zhranklin on 2017/5/15.
 */
object ACMComponents {
  import autowire._

  object list {
    import com.zhranklin.homepage.Dtos._
    import com.zhranklin.homepage.Apis.AcmApi
    import com.zhranklin.homepage.client.MyClient
    import scalaz._
    import Scalaz.{^ ⇒ xxx, _}

    object headingComp {

      val sender = broadcaster

      type Props = Unit
      type State = String
      class Backend($: BackendScope[Props, State]) {
        def render(s: State) =
        <.div(
          MainApp.generalHeading("SOJ索引"),
          <.input(^.cls := "form-control form-control-lg", ^.`type` := "text", ^.placeholder := "id/title",
            ^.value := s,
            ^.onChange ==> pipeInput($)(text ⇒ _ ⇒ text).map(_ >>= sender.change)))
      }
      val comp = ScalaComponent.builder[Props]("ProblemSearchBar")
        .initialState[State]("")
        .backend(new Backend(_))
        .renderBackend
        .build
      def apply() = comp()
    }

    val PROBLEM_MAX = 300

    case class Props(b: Broadcaster[State], all: Array[ProblemItem], ctl: RouterCtl[Page])
    type State = String

    def broadcaster = new BC
    class Backend($: BackendScope[Props, State]) extends OnUnmount
    class BC extends Broadcaster[State] {
      def change(filter: State) = broadcast(filter.toLowerCase)
    }

    val sender = MainApp.broadcaster
    val component = ScalaComponent.builder[Props]("Problems")
      .initialState[State]("")
      .backend(new Backend(_))
      .render_PS { (p, state) ⇒
        <.table(^.cls := "table table-striped",
          <.thead(
            <.tr(<.th("#"), <.th("title"), <.th("submit"), <.th("solved"))),
          <.tbody(p.all.filter(item ⇒ (item.title.toLowerCase contains state) || (item.id contains state)).take(PROBLEM_MAX).toTagMod(item ⇒
            <.tr(<.th(item.id), <.td(<.a(item.title, ^.href := "#", p.ctl setOnClick Page.ACM.Detail(item.id))), <.td(item.submit), <.td(item.solved)))))
      }
      .configure(Listenable.listen(_.b, $ ⇒ $.setState(_: State)))
      .build

    def apply(ctl: RouterCtl[Page]) = AsyncVdom.future(
      MyClient[AcmApi].list().call().map(_.toArray).map {ps ⇒
        component(Props(headingComp.sender, ps, ctl))
      }
    )

  }

  object detail {
    val sender = MainApp.broadcaster

    val comp = ScalaComponent.builder[Problem]("ProblemDetail")
    .render_P { p ⇒
      <.div(^.dangerouslySetInnerHtml := p.html)
    }
    .configure(DidRender.did_P(p ⇒ sender.heading(p.title)))
    .build

    def apply(id: String) = MainApp.Body(
      AsyncVdom.future(MyClient[AcmApi].detail(id).call().map(comp(_))),
      sender
    )


  }

  object sidebar {
    val MAX = 40
    def apply(ctl: RouterCtl[Page]) = AsyncVdom.future(
      MyClient[AcmApi].list().call().map(_.toArray).map { ps ⇒
        <.div(
          <.table(^.cls := "table",
            <.tbody(ps.sortBy(-_.solved).take(MAX).toTagMod(item ⇒
              <.tr(<.th(item.id), <.td(<.a(item.title, ^.href := "#", ctl setOnClick Page.ACM.Detail(item.id))), <.td(item.submit)))))
        )
      }
    )
  }

}

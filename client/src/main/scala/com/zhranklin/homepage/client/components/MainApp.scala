package com.zhranklin.homepage.client.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.component.builder.Lifecycle.StateRW
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.extra.{Broadcaster, OnUnmount}

/**
 * Created by Zhranklin on 2017/4/1.
 */
object MainApp {

  class BC extends Broadcaster[Event] {
    def heading(h: VdomElement): Callback = broadcast(Event.ChangeHeading(h))
    def heading(h: String): Callback = broadcast(Event.ChangeHeading(generalHeading(h)))
  }

  def broadcaster = new BC

  JS.marked.setOptions(JsObj.literal(
    renderer = JsObj.newInstance(JS.marked.Renderer)(),
    valgfm = true,
    tables = true,
    breaks = false,
    pedantic = false,
    sanitize = true,
    smartLists = true,
    smartypants = false
  ))

  def generalHeading(title: String) = <.h1(^.cls := "display-4", title)

  case class Props(ctl: RouterCtl[Page], nav: String, heading: VdomElement, sidebar: VdomElement, body: Body)
  case class State(heading: VdomElement)
  class Backend($: BackendScope[Props, State]) extends OnUnmount

  val comp = ScalaComponent.builder[Props]("MainApp")
    .initialStateFromProps(p ⇒ State(p.heading))
    .backend(new Backend(_))
    .render_PS { (props, state) ⇒
      <.div(
        Nav(props.ctl, props.nav),
        <.div(^.cls := "container",
          <.div(^.cls := "row",
            <.div(^.cls := "col-md-12",
              <.h1(^.cls := "display-1", <.br()),
              state.heading))),
        <.hr,
        <.div(^.cls := "container",
          <.div(^.cls := "row",
            <.div(^.cls := "col-md-9", props.body.body),
            <.div(^.cls := "col-md-3", props.sidebar))),
        <.hr,
        <.footer(
          <.div(^.cls := "container",
            <.div(^.cls := "row",
              <.div(^.cls := "col-sm-12",
                <.p(^.cls := "pull-left",
                  "Powered by",
                  <.a(^.href := "http://www.aliyun.com/?ref=3", ^.target := "_blank", "阿里云sss"))),
              <.div(^.cls := "col-sm-12",
                <.div(^.width := 300.px, ^.margin := "0 auto", ^.padding := "20px 0",
                  <.a(^.target := "_blank", ^.href := "http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33032802000103",
                    ^.display := "inline-block", ^.textDecoration := "none", ^.height := 20.px, ^.lineHeight := 20.px,
                    <.img(^.src := "/img/beian_icon.png", ^.float := "left"),
                    <.p(^.float := "left", ^.height := 20.px, ^.lineHeight := 20.px, ^.margin := "0px 0px 0px 5px", ^.color := "#939393",
                      "浙公网安备 33032802000103号"))))))))
    }
    .configure(Listenable.listen((p: Props) ⇒ p.body.eventSource, handleEvent))
    .componentWillReceiveProps { $ ⇒
      $.setState(State($.nextProps.heading))
    }
    .build

  def handleEvent[B] = ($: StateRW[Props, State, B]) ⇒ (e: Event) ⇒ e match {
    case Event.ChangeHeading(heading) ⇒ $.modState(_.copy(heading = heading))
  }

  case class Body(body: VdomElement, eventSource: Broadcaster[Event] = new Broadcaster[Event] {})
  trait Event
  object Event {
    case class ChangeHeading(heading: VdomElement) extends Event
  }


  def apply(ctl: RouterCtl[Page], nav: String, heading: VdomElement, sidebar: VdomElement, body: VdomElement): VdomElement =
    apply(ctl, nav, heading, sidebar, Body(body))

  def apply(ctl: RouterCtl[Page], nav: String, heading: VdomElement, sidebar: VdomElement, body: Body): VdomElement =
    comp(Props(ctl, nav, heading, sidebar, body))

  val navTitle = new {
    val home = "Home"
    val tech = "Tech"
    val search = "Search"
    val notice = "Notice"
  }
  def apply(ctl: RouterCtl[Page], res: Resolution[Page]): VdomElement = res.page match {
    case Page.Home ⇒
      apply(ctl, navTitle.home, generalHeading("Zhranklin's Blog - Index"), ArticleComponents.sidebar(ctl), ArticleComponents.index(ctl))
    case Page.Article(id) ⇒
      apply(ctl, navTitle.home, generalHeading("article"), ArticleComponents.sidebar(ctl), ArticleComponents.detail(id))
    case Page.Edit(id) ⇒
      apply(ctl, navTitle.home, generalHeading("edit"), ArticleComponents.sidebar(ctl), ArticleComponents.editor(id))
    case Page.Search ⇒
      window.location.href = "/solr"
      null
    case Page.Notice ⇒
      window.location.href = "/notice"
      null
  }


}

package com.zhranklin.homepage.client.components

import com.zhranklin.homepage.client._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import org.scalajs.dom
import rx._

/**
 * Created by Zhranklin on 2017/4/1.
 */
object Layout {
  case class VdomWithCallback(e: VdomElement, cb: Callback = Callback.empty, name: String = "default")

  val heading: Var[VdomElement] = Var(<.div)
  val sidebar: Var[VdomElement] = Var(<.div)
  val body: Var[VdomWithCallback] = Var(VdomWithCallback(<.p("loading...")))
  val navActive = Var("home")

  private val cr: Var[(RouterCtl[Page], Resolution[Page])] = Var((null, null))
  private val ctl = Rx(cr()._1)
  private val res = Rx(cr()._2)
  res.triggerLater{
    body() = VdomWithCallback(<.p("loading..."))
    ArticleComponents.loadSidebar(ctl.now)
    res.now.page match {
      case Page.Home ⇒
        navActive() = navTitle.home
        ArticleComponents.loadArticleList(ctl.now)
      case Page.Article(id) ⇒
        ArticleComponents.loadArticle(id)
      case Page.Tech ⇒
        navActive() = navTitle.tech
        ArticleComponents.loadArticleList(ctl.now)
      case Page.Search ⇒
        navActive() = navTitle.search
      case Page.Notice ⇒
        navActive() = navTitle.notice
      case Page.NewEdit ⇒
        ArticleComponents.loadEditor(None)
      case Page.Edit(id) ⇒
        println("edit...")
        ArticleComponents.loadEditor(id)
    }
  }

  def generalHeading(title: String) = <.h1(^.cls := "display-4", title)

  val navTitle = new {
    val home = "Home"
    val tech = "Tech"
    val search = "Search"
    val notice = "Notice"
  }

  def nav(ctl: RouterCtl[Page]) = {
    def item(title: String, target: Page) =
      <.li(^.classSet1("nav-item", "active" → (navActive.now == title)),
        <.a(^.cls := "nav-link", ctl setOnClick target, ^.href := "#",
          title + " ",
          <.span(^.cls := "sr-only", "(current)")))
    <.nav(^.cls := "navbar navbar-fixed-top navbar-dark bg-inverse",
      <.a(^.cls := "navbar-brand", ctl setOnClick Page.Home, "Zhranklin's Blog", ^.href := "#"),
      <.ul(^.cls := "nav navbar-nav",
        item(navTitle.home, Page.Home),
        item(navTitle.tech, Page.Tech),
        item(navTitle.search, Page.Search),
        item(navTitle.notice, Page.Notice)))
  }

  private val anApp = Rx {
    println("anApp: " + body().name)
    VdomWithCallback(
    <.div(
      nav(ctl()),
      <.div(^.cls := "container",
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-12",
            <.h1(^.cls := "display-1", <.br()),
            heading()))),
      <.hr,
      <.div(^.cls := "container",
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-9", body().e),
          <.div(^.cls := "col-md-3", sidebar()))),
      <.hr,
      <.footer(
        <.div(^.cls := "container",
          <.div(^.cls := "row",
            <.div(^.cls := "col-sm-12",
              <.p(^.cls := "pull-left",
                "Powered by",
                <.a(^.href := "http://www.aliyun.com/?ref=3", ^.target := "_blank", "阿里云"))),
            <.div(^.cls := "col-sm-12",
              <.div(^.width := 300.px, ^.margin := "0 auto", ^.padding := "20px 0",
                <.a(^.target := "_blank", ^.href := "http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33032802000103",
                  ^.display := "inline-block", ^.textDecoration := "none", ^.height := 20.px, ^.lineHeight := 20.px,
                  <.img(^.src := "/img/beian_icon.png", ^.float := "left"),
                  <.p(^.float := "left", ^.height := 20.px, ^.lineHeight := 20.px, ^.margin := "0px 0px 0px 5px", ^.color := "#939393",
                    "浙公网安备 33032802000103号")))))))),
    body().cb, body().name)
  }

  val App = ScalaComponent.builder[VdomWithCallback]("App")
    .initialState_P(identity)
    .backend(identity)
    .render_S { vwc ⇒
      println("render_S: " + vwc.name)
      dom.console.log(vwc.e.rawElement)
      vwc.e
    }
    .componentWillMount(wm ⇒ Callback {
      anApp.triggerLater {
        println("componetWillMount: " + anApp.now.name)
        wm.setState(anApp.now).runNow()
      }
    })
    .componentDidMount(vwc ⇒ Callback {
      println("componentDidMount")
      vwc.state.cb.runNow()
    })
    .componentDidUpdate(vwc ⇒ Callback {
      println("componentDidUpdate")
      vwc.currentState.cb.runNow()
    })
    .build

  val xApp = App(VdomWithCallback(<.p()))

  def apply(c: RouterCtl[Page], r: Resolution[Page]) = {
    cr() = (c, r)
    xApp
  }

}

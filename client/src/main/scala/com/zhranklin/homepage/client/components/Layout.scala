package com.zhranklin.homepage.client.components

import com.zhranklin.homepage.client._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Created by Zhranklin on 2017/4/1.
 */
object Layout {
  val heading: Var[VdomElement] = Var(<.div)
  val sidebar: Var[VdomElement] = Var(<.div)
  val body: Var[VdomElement] = Var(<.p("loading..."))

  private val cr: Var[(RouterCtl[Page], Resolution[Page])] = Var((null, null))
  private val ctl = Rx(cr()._1)
  private val res = Rx(cr()._2)
  res.triggerLater{
    body() = <.p("loading...")
    ArticleComponents.loadSidebar(ctl.now)
    res.now.page match {
      case Home ⇒
        ArticleComponents.loadArticleList(ctl.now)
      case Article(id) ⇒
        ArticleComponents.loadArticle(id)
    }
  }

  def generalHeading(title: String) = <.h1(^.cls := "display-4", title)

  def nav(ctl: RouterCtl[Page]) = {
    def item(text: String, target: Page) =
      <.li(^.cls := "nav-item active",
        <.a(^.cls := "nav-link", ctl setOnClick target,
          text + " ",
          <.span(^.cls := "sr-only", "(current)")))
    <.nav(^.cls := "navbar navbar-fixed-top navbar-dark bg-inverse",
      <.a(^.cls := "navbar-brand", ctl setOnClick Home, "Zhranklin's Blog"),
      <.ul(^.cls := "nav navbar-nav",
        item("Home", Home)))
  }

  private val anApp = Rx {
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
          <.div(^.cls := "col-md-9", body()),
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
                    "浙公网安备 33032802000103号"))))))))
  }

  val App = ScalaComponent.builder[VdomElement]("App")
    .initialState_P(identity)
  .backend(identity)
  .render_S(identity)
  .componentWillMount(wm ⇒ Callback {
    anApp.triggerLater {
      wm.setState(anApp.now).runNow
    }
  }).build

  def apply(c: RouterCtl[Page], r: Resolution[Page]) = {
    cr() = (c, r)
    App(anApp.now)
  }

}

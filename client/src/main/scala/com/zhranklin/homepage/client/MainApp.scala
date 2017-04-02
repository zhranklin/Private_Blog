package com.zhranklin.homepage.client

import com.zhranklin.homepage.client.components.{ArticleComponents, Layout}
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, ScalaComponent}
import org.scalajs.dom

import scala.scalajs.js

sealed trait Page
case object Home extends Page
case object Tech extends Page
case class Article(id: String) extends Page

trait WithTitle { self: Page ⇒
  val title: String
  def renderTitle = <.h1(^.cls := "display-4", title)
}

object MainApp extends js.JSApp {

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl ⇒
    import dsl._
    val R = render(null)
    (trimSlashes
      | staticRoute(root, Home) ~> R
      | dynamicRouteCT("article" / string("[a-fA-F0-9]+").caseClass[Article]) ~> R
      ).notFound(_ ⇒ redirectToPage(Home)(Redirect.Replace))
      .renderWith(Layout(_,_))
  }

  def main(): Unit = {

    val baseUrl = BaseUrl.fromWindowOrigin / "react/"
    val mountPoint = dom.document.getElementById("root")
    val router: ScalaComponent[Unit, Resolution[Page], OnUnmount.Backend, CtorType.Nullary] = Router(baseUrl, routerConfig)

    router().renderIntoDOM(mountPoint)
  }
}


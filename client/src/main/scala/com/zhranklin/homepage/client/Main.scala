package com.zhranklin.homepage.client

import com.zhranklin.homepage.client.components.MainApp
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router.StaticDsl._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, ScalaComponent}
import org.scalajs.dom

import scala.reflect.ClassTag
import scala.scalajs.js
import scala.scalajs.js.|

sealed trait Page
object Page {
  case object Home extends Page
  case object Tech extends Page
  case class Article(id: String) extends Page
  case class Edit(id: Option[String]) extends Page
  case object Search extends Page
  case object Notice extends Page
  object ACM {
    case object List extends Page
    case class Detail(id: String) extends Page
  }
}

object Main extends js.JSApp {

  val navTitle = new {
    val home = "Home"
    val tech = "Tech"
    val search = "Search"
    val notice = "Notice"
  }
  import Page._

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl ⇒
    import dsl._
    def staticRoute(r: Route[Unit], page: Page) = dsl.staticRoute(r, page).~>(render(null))
    def dynamicRouteCT[P <: Page](r: Route[P])(implicit ct: ClassTag[P]) = dsl.dynamicRouteCT(r).~>(render(null))
    (trimSlashes
      | staticRoute(root, Home)
      | staticRoute("tech", Tech)
      | staticRoute("search", Search)
      | staticRoute("notice", Notice)
      | staticRoute("acm", ACM.List)
      | dynamicRouteCT("acm" / string("[0-9]+").caseClass[ACM.Detail])
      | dynamicRouteCT("article" / string("[a-fA-F0-9]+").caseClass[Article])
      | dynamicRouteCT("editor" ~ ("/" ~ string("[a-fA-F0-9]+")).option.caseClass[Edit])
      ).notFound(_ ⇒ redirectToPage(Home)(Redirect.Replace))
    .renderWith(MainApp(_, _))
  }

  def main(): Unit = {

    val baseUrl = BaseUrl.fromWindowOrigin / "react/"
    val mountPoint = dom.document.getElementById("root")
    val router: ScalaComponent[Unit, Resolution[Page], OnUnmount.Backend, CtorType.Nullary] = Router(baseUrl, routerConfig)

    router().renderIntoDOM(mountPoint)
  }
}


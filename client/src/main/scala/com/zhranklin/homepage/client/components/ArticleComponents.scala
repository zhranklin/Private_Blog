package com.zhranklin.homepage.client.components

import autowire._
import com.zhranklin.homepage.Apis.ArticleApi
import com.zhranklin.homepage.Dtos.ArticleItem
import com.zhranklin.homepage.client._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Created by Zhranklin on 2017/3/31.
 */
object ArticleComponents {

  def articleItem(a: ArticleItem, ctl: RouterCtl[Page]) =
    <.div(
      <.div(^.cls := "row",
        <.br,
        <.div(^.cls := "col-md-12",
          <.h3(a.title),
          <.div(^.cls := "row",
            <.div(^.cls := "col-md-12",
              <.p(a.abs, "..."),
              <.p(^.cls := "lead", <.button(^.cls := "btn btn-default", ctl setOnClick Article(a.id), "Read More")),
              <.p(^.cls := "pull-right",
                a.tags.toTagMod(t ⇒ <.span(^.cls := "label label-default", t))),
              <.ul(^.cls := "list-inline",
                <.li(<.a(a.create_time)))),
            <.div(^.cls := "col-xs-3")),
          <.br,
          <.br)),
      <.hr)

  def loadArticleList(ctl: RouterCtl[Page]) = {
    Layout.heading() = Layout.generalHeading("Zhranklin's Blog - Index")
    MyClient[ArticleApi].list().call().foreach { articles ⇒
      Layout.body() =
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-12",
            <.div(^.cls := "panel",
              <.div(^.cls := "panel-body",
                articles.toTagMod(a ⇒ articleItem(a, ctl)),
                <.a(^.href := "/", ^.cls := "btn btn-primary pull-right btnNext",
                  "More", <.i(^.cls := "glyphicon glyphicon-chevron-right"))))))
    }
  }

  def loadSidebar(ctl: RouterCtl[Page]) = MyClient[ArticleApi].list().call().foreach { articles ⇒
    Layout.sidebar() = <.div(
      <.p("文章列表"),
      <.ul(^.cls := "nav",
        articles.toTagMod(a ⇒
          <.li(<.a(ctl setOnClick Article(a.id), a.title)))))
  }

  def loadArticle(id: String) =
    MyClient[ArticleApi].get(id).call().foreach(_.foreach { a ⇒
      Layout.heading() = Layout.generalHeading(a.title)
      Layout.body() =
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-12", ^.id := "article", ^.dangerouslySetInnerHtml := "<h1></h1>" + a.html))
    })
}

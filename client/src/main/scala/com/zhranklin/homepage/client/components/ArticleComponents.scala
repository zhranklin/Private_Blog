package com.zhranklin.homepage.client.components

import autowire._
import com.zhranklin.homepage.Apis.ArticleApi
import com.zhranklin.homepage.Dtos.{ArticleEdit, ArticleItem}
import com.zhranklin.homepage.client._
import com.zhranklin.homepage.client.components.Layout.VdomWithCallback
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Created by Zhranklin on 2017/3/31.
 */
object ArticleComponents {

  import Imports._

  def loadArticleList(ctl: RouterCtl[Page]) = {
    def articleItem(a: ArticleItem, readmore: TagMod, edit: TagMod) =
      <.div(
        <.div(^.cls := "row",
          <.br,
          <.div(^.cls := "col-md-12",
            <.h3(a.title),
            <.div(^.cls := "row",
              <.div(^.cls := "col-md-12",
                <.p(a.abs, "..."),
                <.p(^.cls := "lead", <.button(^.cls := "btn btn-default", readmore, "Read More")),
                <.p(^.cls := "lead", <.button(^.cls := "btn btn-default", edit, "Edit")),
                <.p(^.cls := "pull-right",
                  a.tags.toTagMod(t ⇒ VdomArray(<.span(^.cls := "label label-default", t), " "))),
                <.ul(^.cls := "list-inline",
                  <.li(<.a(a.create_time)))),
              <.div(^.cls := "col-xs-3")),
            <.br,
            <.br)),
        <.hr)
    Layout.heading() = Layout.generalHeading("Zhranklin's Blog - Index")
    MyClient[ArticleApi].list().call().foreach { articles ⇒
      Layout.body() = VdomWithCallback(
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-12",
            <.div(^.cls := "panel",
              <.div(^.cls := "panel-body",
                articles.toTagMod(a ⇒ articleItem(a, ctl setOnClick Page.Article(a.id), ctl setOnClick Page.Edit(Some(a.id)))),
                <.a(^.href := "/", ^.cls := "btn btn-primary pull-right btnNext",
                  "More", <.i(^.cls := "glyphicon glyphicon-chevron-right")))))))
    }
  }

  def loadSidebar(ctl: RouterCtl[Page]) = MyClient[ArticleApi].list().call().foreach { articles ⇒
    Layout.sidebar() = <.div(
      <.p("文章列表"),
      <.ul(^.cls := "nav",
        articles.toTagMod(a ⇒
          <.li(<.a(^.href := "#", ctl setOnClick Page.Article(a.id), a.title)))))
  }

  def loadArticle(id: String) =
    MyClient[ArticleApi].get(id).call().foreach(_.foreach { a ⇒
      Layout.heading() = Layout.generalHeading(a.title)
      Layout.body() = VdomWithCallback(
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-12", ^.id := "article", ^.dangerouslySetInnerHtml := "<h1></h1>" + a.html)))
    })
  
  def loadEditor(id: Option[String]) = {
    def editorTemplate(article: Option[ArticleEdit]) =
      <.form(^.action := "#", ^.id := "form",
        <.div(^.cls :="container",
          <.div(^.cls :="row",
            <.div(^.cls :="col-md-5",
              <.label(^.`for` := "tags", "标签："),
              <.input(^.`type` := "text", ^.id := "tags", article.map(a ⇒ ^.defaultValue := a.tags mkString ", ").whenDefined)),
            <.div(^.cls := "col-md-5",
              <.label(^.`for` :="tags", "板块："),
              <.input(^.`type` :="text", ^.id := "section", article.map(a ⇒ ^.defaultValue := a.section).whenDefined)),
            <.div(^.cls := "col-md-2",
              <.p( ^.cls :="lead",
                <.button(^.cls := "btn btn-default", ^.id := "editor-submit", "提交"))))),
        <.textarea(^.id := "editor"))

    val articleOpt = id.map { id ⇒
      MyClient[ArticleApi].get(id).call()
    }
    println("ssss1")
    val callback = Callback {
      println("ssss")
      val mde = JsObj.newInstance(JsObj.global.SimpleMDE)(JsObj.literal(
        element = JS.document.getElementById("editor"),
        spellChecker = false,
        autosave = JsObj.literal(
          enabled = true,
          unique_id = "content"
        ),
        renderingConfig = JsObj.literal(
          singleLineBreaks = false,
          codeSyntaxHighlighting = true
        )))
      articleOpt.foreach(_.map(_.map(a ⇒ mde.value(s"# ${a.title}\n${a.mdown.get}"))))
      $("#editor-submit").click { e: Any ⇒
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
        val mdRaw = mde.value().asInstanceOf[String]
        val index = mdRaw.indexOf('\n')
        var (titleWithSharp, mdContent) = mdRaw.splitAt(index)
        val html = JS.marked(mdContent).asInstanceOf[String]
        MyClient[ArticleApi].save(id, ArticleEdit(
          if (titleWithSharp startsWith "#") titleWithSharp.drop(1).trim else titleWithSharp.trim,
          "Zhranklin",
          $("#section").`val`().asInstanceOf[String],
          Some(mdContent),
          html,
          html.replaceAll("<.*?>", " ").replaceAll("\\s\\s+", " ").take(200),
          $("#tags").`val`().asInstanceOf[String].trim.split("\\s*,\\s*").toList
        )).call().foreach(u ⇒ window.alert("保存成功"))
      }
    }

    articleOpt.map{ a ⇒
      println("3" + a)
      a.map{ a ⇒
        println("1" + a)
        a.map{ a ⇒
          println("2" + a)
          Layout.body() = VdomWithCallback(editorTemplate(Some(a)), callback, "editor1")
        }
      }
    }.getOrElse(Layout.body() = VdomWithCallback(editorTemplate(None), callback, "editor2"))

  }
}

package com.zhranklin.homepage.client.components

import autowire._
import com.zhranklin.homepage.Apis.ArticleApi
import com.zhranklin.homepage.Dtos.{ArticleEdit, ArticleItem}
import com.zhranklin.homepage.client._
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom
import org.scalajs.dom

import scalaz._

/**
 * Created by Zhranklin on 2017/3/31.
 */
object ArticleComponents {

  def articleHeading(title: String, tags: List[String]): VdomElement =
  <.div(
    <.h1(title),
      <.p(^.cls := "pull-left",
        tags.toTagMod(tag ⇒ vdom.TagMod(<.span(^.cls := "label label-default", tag), " "))
      ))

  def index(ctl: RouterCtl[Page]) = {
    def item(a: ArticleItem, readMoreLink: TagMod) =
      <.div(
        <.div(^.cls := "row",
          <.br,
          <.div(^.cls := "col-md-12",
            <.h3(a.title),
            <.div(^.cls := "row",
              <.div(^.cls := "col-md-12",
                <.p(a.abs, "..."),
                <.p(^.cls := "lead", <.button(^.cls := "btn btn-default", readMoreLink, "Read More")),
                <.p(^.cls := "pull-right",
                  a.tags.toTagMod(t ⇒ TagMod(<.span(^.cls := "label label-default", t), " "))),
                <.ul(^.cls := "list-inline",
                  <.li(<.a(a.create_time)))),
              <.div(^.cls := "col-xs-3")),
            <.br,
            <.br)),
        <.hr)
    AsyncVdom.future(MyClient[ArticleApi].list().call().map { articles ⇒
      <.div(^.cls := "row",
        <.div(^.cls := "col-md-12",
          <.div(^.cls := "panel",
            <.div(^.cls := "panel-body",
              articles.toTagMod(a ⇒ item(a, ctl setOnClick Page.Article(a.id))),
              <.a(^.href := "/", ^.cls := "btn btn-primary pull-right btnNext",
                "More", <.i(^.cls := "glyphicon glyphicon-chevron-right"))))))
    })
  }

  def sidebar(ctl: RouterCtl[Page]) = {
    AsyncVdom.future(
      MyClient[ArticleApi].list().call().map{ articles ⇒
        <.div(
          <.p("文章列表"),
          <.ul(^.cls := "nav",
            articles.toTagMod(a ⇒
              <.li(<.a(^.href := "#", ctl setOnClick Page.Article(a.id), a.title)))))
      }
    )
  }

  object detail {
    val sender = MainApp.broadcaster

    val highlight = Callback {
        window.console.log("bbb")
        $("pre code").each{ (block: dom.Element) ⇒
          window.console.log(block)
          JS.hljs.highlightBlock(block)
        }
      }

    val comp = ScalaComponent.builder[Option[ArticleEdit]]("Detail")
      .render_P (_.map { article ⇒
        val html = JS.marked(article.mdown.get).asInstanceOf[String]
        <.div(^.dangerouslySetInnerHtml := html): VdomElement
      }.getOrElse(<.div(<.p("未找到该文章"))))
      .configure(DidRender.did_P(_.map(a ⇒ articleHeading(a.title, a.tags)).foreachCb(sender.heading) >> highlight))
      .build

    def apply(id: String) = MainApp.Body(
      AsyncVdom.future(
        MyClient[ArticleApi].get(id).call().map {comp(_)}
      )
      , sender)
  }

  object editor {

    val sender = MainApp.broadcaster

    case class State(edit: ArticleEdit, tagsText: String, loadMarkdown: Boolean)
    type Props = Option[String]
    def mkTagsText(tags: List[String]) = tags mkString ", "

    val init = State(ArticleEdit("", "Zhranklin", "tech", Some(""), "", "", Nil), "", true)
    val stateLens = Lens.lensu[State, ArticleEdit]((s, a) ⇒ s.copy(edit = a, loadMarkdown = false), _.edit)
    val tagTextLens = Lens.lensu[State, String]((s, tt) ⇒ s.copy(tagsText = tt, loadMarkdown = false), _.tagsText)
    val tagLens = stateLens andThen Lens.lensu[ArticleEdit, List[String]]((a, s) ⇒ a.copy(tags = s), _.tags)
    val section = stateLens andThen Lens.lensu[ArticleEdit, String]((a, s) ⇒ a.copy(section = s), _.section)
    val content = stateLens andThen Lens.lensu[ArticleEdit, (String, String)]((a, s) ⇒ a.copy(title = s._1, mdown = Some(s._2)), a ⇒ (a.title, a.mdown.get))
    var mde: JsObj = _

    case class Backend($: BackendScope[Props, State]) {

      def submit = Callback {
        val state = $.state.runNow()
        val (title, md) = content.get(state)
        val abs = JS.marked(md).asInstanceOf[String].replaceAll("<.*?>", " ").replaceAll("\\s\\s+", " ").take(200)
        MyClient[ArticleApi].save($.props.runNow(), state.edit.copy(abs = abs)).call()
          .foreach(u ⇒ window.alert("保存成功"))
      }

      def render(state: State) =
        <.form(^.action := "#",
          <.div(^.cls :="container",
            <.div(^.cls :="row",
              <.div(^.cls :="col-md-5",
                <.label(^.`for` := "tags", "标签："),
                <.input(^.`type` := "text", ^.value := state.tagsText,
                  ^.onChange ==> handleInput($)(text ⇒
                    ((_: State).copy(tagsText = text))
                      andThen (tagLens.set(_, text.split("""\s*,\s*""").toList))),
                  ^.onBlur ==> handleInput($)(text ⇒
                    (tagLens.mod(_.map(_.trim).distinct.sorted.filterNot(_==""), _: State))
                      andThen (st ⇒ tagTextLens.set(st, mkTagsText(tagLens.get(st))))))),
              <.div(^.cls := "col-md-5",
                <.label(^.`for` := "tags", "板块："),
                <.input(^.`type` := "text", ^.value := section.get(state),
                  ^.onChange ==> handleInput($)(text ⇒ section.set(_,text)))),
              <.div(^.cls := "col-md-2",
                <.p( ^.cls :="lead",
                  <.button(^.cls := "btn btn-default", ^.onClick --> submit, "提交"))))),
          <.textarea(^.id := "editor"))
    }

    val component = ScalaComponent.builder[Props]("Editor")
      .initialState[State](init)
      .renderBackend[Backend]
      .componentWillMount { $ ⇒
        $.props.foreachCb { id ⇒
          Callback.future {
            MyClient[ArticleApi].get(id).call().map { edit ⇒
              sender.heading(edit.map(_.title).getOrElse("Editing")) >>
              $.setState(edit.map(a ⇒ State(a, mkTagsText(a.tags), true)).getOrElse(init))
            }
          }
        }
      }
      .componentDidMount { cdm ⇒
        Callback {
          mde = JsObj.newInstance(JsObj.global.SimpleMDE)(JsObj.literal(
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
          mde.codemirror.on("change", () ⇒ {
            val text = mde.value().asInstanceOf[String]
            var (titleWithSharp, md) = text.splitAt(text.indexOf('\n'))
            val title = if (titleWithSharp startsWith "# ") titleWithSharp.drop(2) else titleWithSharp
            cdm.modState(content.set(_, (title, md))).runNow()
          })
        }
      }
      .componentDidUpdate { $ ⇒
        val (title, md) = content.get($.currentState)
        sender.heading(articleHeading(title, tagLens.get($.currentState))) >>
        Callback {
          if ($.currentState.loadMarkdown) {
            mde.value(s"# $title\n$md")
          }
        }
      }
      .build

    def apply(id: Props) = MainApp.Body(component(id), sender)

  }


}

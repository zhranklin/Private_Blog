package com.zhranklin.homepage.blog

import java.util.Date

import akka.http.scaladsl.model.{ContentType, HttpEntity}
import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.Apis.ArticleApi
import com.zhranklin.homepage.Dtos.{ArticleEdit, ArticleItem}
import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.blog.db._
import org.bson.types.ObjectId
import upickle.Js
import upickle.default._

import scala.util.Try

trait BlogRoute extends RouteService with ArticleApi {
  import com.zhranklin.homepage.ActorImplicits._

  def asItem(a: Article) = ArticleItem(a.id.get.toHexString, a.title, a.author, a.create_time.dateString, a.abs, a.tags)

  def asEdit(a: Article) = ArticleEdit(a.title, a.author, a.section, a.mdown, a.html, a.abs, a.tags)

  def list() = articleList(None).map(asItem)

  def get(id: String): Option[ArticleEdit] = for {
    bid ← Try(new ObjectId(id)).toOption //验证id这个字符串是否符合ObjectId构造器的要求,如果不符合则可认为是新文章
    mongo ← articles.findOneByID(bid)
  } yield asEdit(new Article(mongo))

  abstract override def myRoute = super.myRoute ~
    (post & path("api" / Segments)) { s ⇒
      (extract(_.request.entity match {
        case HttpEntity.Strict(nb: ContentType.NonBinary, data) =>
          data.decodeString(nb.charset.value)
      })) { e ⇒
        complete {
          Autowire.route[ArticleApi](this)(
            autowire.Core.Request(
              s,
              upickle.json.read(e).asInstanceOf[Js.Obj].value.toMap
            )
          ).map(upickle.json.write(_))
        }
      }
    } ~
    path("section" / Segment) { section ⇒
      complete {
        html.index.render(s"Zhranklin's blog - $section", section, articleList(Some(section)))
      }
    } ~
    path("") {
      complete(html.index.render("Zhranklin's blog - home", "home", articleList(None)))
    } ~
    path("refresh") {
      complete {
        refreshArticleList()
        html.message.render("Info", "刷新成功.")
      }
    } ~
    path("blog" / Segment) { s =>
      complete {
        val str = s.replaceAll("\\+", "%2B")
        html.article.render(articles.findOne($("title" -> decode(str))).get)
      }
    } ~
    pathPrefix("editor" / Segment) { token ⇒
      (if (token != getToken)
        complete("invalid token!")
      else reject) ~
      (pathPrefix("submit") & post & formField('id, 'title, 'html, 'markdown, 'tags, 'section)) {
        (id, title, content, markdown, tags, section) => complete {
          val (createTime, bid) = getCreateTimeAndBid(id)
          val art = createArticle(title, section, content, markdown, tags, createTime)
          if (bid.nonEmpty)
            articles.update("_id" $eq bid.get, art.mongo)
          else
            articles.insert(art.mongo)
          refreshArticleList()
          html.message.render("Info", "修改/添加成功.")
        }
      } ~
      pathPrefix(Segment) { title ⇒
        complete {
          val ar: Option[Article] =
            if (title == "") None
            else {
              val t = title.replaceAll("\\+", "%2B")
              for {
                article <- articles.findOne($("title" -> java.net.URLDecoder.decode(t, "UTF-8")))
                md <- article.mdown
              } yield article
            }
          if (ar.isEmpty)
            html.message.render("Error", "该文章不存在或无法编辑.")
          else
            html.editor.render(ar)
        }
      } ~
      pathEnd {
        complete(html.editor.render(None))
      }
    } ~
    path("2048" / Segment) { text ⇒
      complete {
        val textList = text.split("\\+").toList.map(decode)
        html.m2048.render(textList)
      }
    }

  object Autowire extends autowire.Server[Js.Value, Reader, Writer] {
    def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)
    def write[Result: Writer](r: Result) = upickle.default.writeJs(r)
  }

  private def getCreateTimeAndBid(id: String): (Option[Date], Option[ObjectId]) = {
    val timeAndId = for {
      bid ← Try(new ObjectId(id)).toOption //验证id这个字符串是否符合ObjectId构造器的要求,如果不符合则可认为是新文章
      mongo ← articles.findOneByID(bid)
      createTime ← mongo.getAs[Date]("create_time")
    } yield (Some(createTime), Some(bid))
    timeAndId.getOrElse(None, None)
  }

  private def createArticle(title: String, section: String, content: String, markdown: String,
                            tags: String, createTime: Option[Date]): Article =
    Article(
      title = title.trim.dropWhile(_ == '#').trim, section = section,
      author = "Zhranklin", mdown = Some(markdown), html = content,
      create_time = createTime.getOrElse(new Date),
      abs = content.replaceAll("<.*?>", " ").replaceAll("\\s\\s+", " ").take(200),
      tags = ",".r.split(tags).map(_.trim).toList.filterNot(_ matches "\\s*"))
}

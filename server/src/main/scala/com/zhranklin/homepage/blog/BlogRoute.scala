package com.zhranklin.homepage.blog

import java.util.Date

import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.blog.db._
import org.bson.types.ObjectId

import scala.util.Try

trait BlogRoute extends RouteService {
  abstract override def myRoute = super.myRoute ~
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
      (pathEnd) {
        complete(html.editor.render(None))
      }
    } ~
    path("2048" / Segment) { text ⇒
      complete {
        val textList = text.split("\\+").toList.map(decode)
        html.m2048.render(textList)
      }
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

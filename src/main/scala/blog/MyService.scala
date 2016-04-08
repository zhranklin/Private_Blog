package com.zhranklin.blog

import java.util.Date

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBList => $$, MongoDBObject => $}
import spray.httpx.PlayTwirlSupport._
import spray.routing._
import org.bson.types.ObjectId
import db._

import scala.util.Try

class MyServiceActor extends Actor with MyService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}

trait MyService extends HttpService {

  val myRoute =
    getFromResourceDirectory("") ~
    path("") {
      complete {
        html.index.render("Home", articleList)
      }
    } ~
    path("refresh") {
      complete {
        refreshArticleList()
        html.message.render("Info", "刷新成功.")
      }
    } ~
    path("blog" / Rest) {str =>
      complete {
        html.article.render(articles.findOne($("title" -> java.net.URLDecoder.decode(str, "UTF-8"))).get)
      }
    } ~
   // TODO: 这里的路由(/editor/*)做好设计
      // 理想状态: /editor/submit 文章提交 /editor/ 添加新文章, /editor/name 修改title为name的文章
    path("editor" / "submit") {
      post {
        anyParams('id, 'title, 'html, 'markdown, 'tags) { (id, title, Hhtml, markdown, tags) =>
          complete {
            val (createTime, bid) = {
              val timeAndId = for {
                bid ← Try(new ObjectId(id)).toOption //验证id这个字符串是否符合ObjectId构造器的要求,如果不符合则可认为是新文章
                mongo ← articles.findOneByID(bid)
                createTime ← mongo.getAs[Date]("create_time")
              } yield (Some(createTime), Some(bid))
              timeAndId.getOrElse(None, None)
            }
            val art = Article(
              title = title.trim.dropWhile(_ == '#').trim,
              author = "Zhranklin", mdown = Some(markdown), html = Hhtml,
              create_time = createTime.getOrElse(new Date),
              abs = Hhtml.replaceAll("<.*?>", " ").replaceAll("\\s\\s+", " ").take(200),
              tags = ",".r.split(tags).map(_.trim).toList.filterNot(_ matches "\\s*"))
            if (bid.nonEmpty)
              articles.update("_id" $eq bid.get, art.mongo)
            else
              articles.insert(art.mongo)
            refreshArticleList()
            html.message.render("Info", "修改/添加成功.")
          }
        }
      }
    } ~
    path("editor" / Rest) { title =>
      complete {
        html.editor.render(None)
      }
    } ~
    path("edit" / Rest) { title =>
      complete {
        val arMongo = articles.findOne($("title" -> java.net.URLDecoder.decode(title, "UTF-8")))
        val ar = for {
          article <- arMongo
          md <- article.mdown
        } yield article
        if (ar.isEmpty)
          html.message.render("Error", "该文章不存在或无法编辑.")
        else
          html.editor.render(Some(ar.get))
      }
    }
}

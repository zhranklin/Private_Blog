package com.zhranklin.blog

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject => $$}
import spray.httpx.PlayTwirlSupport._
import spray.routing._

import db._

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
        "successfully refreshed"
      }
    } ~
    path("blog" / Rest) {str =>
      complete {
        html.article.render(articles.findOne($$("title" -> java.net.URLDecoder.decode(str, "UTF-8"))).get)
      }
    } ~
    path("editor") {
      complete {
        html.editor.render()
      }
    } ~
    path("edit" / Rest) { title =>
      complete {
        val ar: Article = articles.findOne($$("title" -> java.net.URLDecoder.decode(str, "UTF-8"))).get
        html.editor.render(Some(ar))
      }
    }
    path("editor" / "submit") {
      post {
        anyParams('title, 'html, 'markdown, 'tags) { (title, html, markdown, tags) =>
          complete {
            val abs = html.replaceAll("<.*>", " ").replaceAll("\\s\\s+", " ").take(200)
            val tagList = ",".r.split(tags).map(_.trim).toList
            val realTitle = title.trim.dropWhile(_ == '#').trim
            articles.insert(Article(title = realTitle, author = "Zhranklin", mdown = Some(markdown), html = html, abs = abs, tags = tagList).mongo)
            refreshArticleList()
            "successfully inserted."
          }
        }
      }
    }
}

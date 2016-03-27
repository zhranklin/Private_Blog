package com.zhranklin.blog

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject => $$}
import spray.httpx.PlayTwirlSupport._
import spray.routing._

import db._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
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

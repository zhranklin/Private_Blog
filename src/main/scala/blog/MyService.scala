package com.zhranklin.blog

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject => $$}
import com.zhranklin.blog.db._
import spray.httpx.PlayTwirlSupport._
import spray.routing._

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
        html.index.render("Home", articles.find.toList map (new Article(_)))
      }
    } ~
    path("blog" / Rest) {str =>
      complete {
        html.article.render(articles.findOne($$("title" -> java.net.URLDecoder.decode(str, "UTF-8"))).get)
      }
    }
}

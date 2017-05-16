package com.zhranklin.homepage.acm

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.mongodb.casbah.MongoClient
import com.zhranklin.homepage.Apis.AcmApi
import com.zhranklin.homepage.Dtos.{Problem, ProblemItem}
import org.jsoup.Jsoup

import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.typesafe.config.ConfigFactory
import com.zhranklin.homepage.Util._
import org.jsoup.nodes.Node
import org.jsoup.select.NodeVisitor

import scala.concurrent.Future
import scala.util.Try

/**
 * Created by zhranklin on 2017/5/15.
 */
object AcmImpl extends AcmApi {
  import com.zhranklin.scalatricks.PostfixToInfix._
  import com.zhranklin.homepage.BasicJsonSupport._
  val host = ConfigFactory.load().getString("settings.server.acm_host")
  val db = MongoClient()("test")("acm_problems")
  def parseProblemPage(i: Int) = {
    val doc = Jsoup.connect(s"$host/problems.action?volume=$i").get()
    doc.select("form table tbody tr:has(td:nth-of-type(5))").asScala.map { tr ⇒
      val List(_, id, title, submit, solved) = 1 to 5 map (i ⇒ tr.select(s"td:nth-of-type($i)").text) toList <>
      ProblemItem(id, title, submit.toInt, solved.toInt)
    } toList <>
  }
  var lastMillis: Long = 0
  def tryAppend() = {
    if (lastMillis + 2000 > System.currentTimeMillis())
      ()
    else {
      lastMillis = System.currentTimeMillis()
      val newId: Int = Try(db.map(_.as[String]("id").toInt).max).getOrElse(1000) + 1
      println(s"newId: $newId")
      val page = newId / 100 - 10
      //    println(s"table before: ${db.find.toList}")
      parseProblemPage(page).foreach{ p ⇒
        db.remove("id" $eq p.id)
        db.insert($(serialization.write(p)))
      }
      //    println(s"table after: ${db.find.toList}")
    }
  }

  import com.zhranklin.homepage.ActorImplicits._
  import akka.http.scaladsl.model.headers._
//  case class Agent(session: String) {
//
//    def mkCookie = Cookie(Agent.JSESSIONID, session) :: Nil
//
//    implicit class WithSession(request: HttpRequest) {
//      def withSession = request.copy(headers = mkCookie)
//    }
//
//    def mkForm(map: Map[String, String]) = HttpEntity(
//      map.map(pair ⇒ s"${encode(pair._1)}=${encode(pair._2)}").mkString("&"))
//
//    def login(id: String, pass: String) = Http().singleRequest(HttpRequest(uri = s"$host/login.action?id=$id&password=$pass").withSession)
//
//    def logout() = Http().singleRequest(HttpRequest(uri = s"$host/logout.action").withSession)
//
//    def submit(id: String, validation: String, lang: String, src: String) =
//      Http().singleRequest(HttpRequest(HttpMethods.POST, s"$host/submit.action", entity = mkForm(Map(
//        "problemId" → id,
//        "validation" → validation,
//        "language" → lang,
//        "submit" → "Submit"))).withSession)
//
//    def submitPic =
//
//  }
//
//  object Agent {
//    val JSESSIONID = "JSESSIONID"
//    def newSession: Future[String] = Http().singleRequest(HttpRequest(uri = s"$host/")).map(_.headers.flatMap {
//      case Cookie(pairs) ⇒
//        pairs.filter(_.name == JSESSIONID)
//      case _ ⇒ Nil
//    }.head.value)
//  }

  def mongoDBToProblemItem(obj: DBObject) =
  ProblemItem(obj.as[String]("id"), obj.as[String]("title"), obj.as[Int]("submit"), obj.as[Int]("solved"))

  def list() = {
    tryAppend()
    db.find().map(mongoDBToProblemItem).toList.sortBy(_.id.toInt)
  }

  def detail(id: String) ={
    val doc = Jsoup.connect(s"$host/problem/$id/").get//.select(".Section1")
    .traverse(new NodeVisitor {
      def head(node: Node, depth: Int) = node.removeAttr("style")
      def tail(node: Node, depth: Int) = ()
    })

    val title = Jsoup.connect(s"$host/problem.action?id=$id").get.select("body > h1").text().split(":").drop(1).mkString(":")
    Problem(title, doc.toString)
  }

}

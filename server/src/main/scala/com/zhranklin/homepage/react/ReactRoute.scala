package com.zhranklin.homepage.react

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.{ContentType, HttpEntity}
import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.Apis.{AcmApi, ArticleApi}
import com.zhranklin.homepage.Dtos.{ArticleEdit, ArticleItem}
import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.acm.AcmImpl
import com.zhranklin.homepage.blog.Article
import org.bson.types.ObjectId
import scalaz.Alpha.S
import upickle.Js
import upickle.default._

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

/**
 * Created by Zhranklin on 2017/3/1.
 */
trait ReactRoute extends RouteService {

  object ArticleImpl extends ArticleApi {
    import com.zhranklin.homepage.blog.db._

    def list() = articleList(None).map{ a ⇒ ArticleItem(a.id.get.toHexString, a.title, a.author, a.create_time.dateString, a.abs, a.tags)}

    def asEdit(a: Article) = ArticleEdit(a.title, a.author, a.section, a.mdown, a.html, a.abs, a.tags)
    def get(id: String): Option[ArticleEdit] = for {
      bid ← Try(new ObjectId(id)).toOption //验证id这个字符串是否符合ObjectId构造器的要求,如果不符合则可认为是新文章
      mongo ← articles.findOneByID(bid)
    } yield asEdit(new Article(mongo))

    def save(id: Option[String], article: ArticleEdit) = {
      def fromEdit(id: Option[ObjectId], a: ArticleEdit) = {
        val create_time = id.flatMap(articles.findOneByID).map(new Article(_).create_time).getOrElse(new Date)
        Article(a.title, a.author, a.section, a.mdown, a.html, a.abs, a.tags, create_time, new Date, id)
      }
      val art = fromEdit(id.map(new ObjectId(_)), article).mongo
      if (id.isEmpty) {
        println("1" + art)
        println("sss")
        articles.insert(art)
        articles.foreach(println)
      }
      else {
        println("2" + art)
        articles.update("_id" $eq new ObjectId(id.get), art)
      }
      refreshArticleList()
    }
  }

  import com.zhranklin.homepage.ActorImplicits._

  val router = {
    import Autowire.route
    List(
      route[ArticleApi](ArticleImpl),
      route[AcmApi](AcmImpl)
    ).reduce(_.orElse(_))
  }

  abstract override def myRoute = super.myRoute ~
    pathPrefix("react") {
      complete(react.html.index.render("Zhranklin's Blog"))
    } ~
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    } ~
    (post & path("api" / Segments) & extractStrictEntity(FiniteDuration(3, TimeUnit.SECONDS))) { (s, e) ⇒
      complete {
        val payload = e match {
          case HttpEntity.Strict(nb: ContentType.NonBinary, data) =>
            data.decodeString(nb.charset.value)
        }
        router(
          autowire.Core.Request(
            s,
            upickle.json.read(payload).asInstanceOf[Js.Obj].value.toMap
          )
        ).map(upickle.json.write(_))
      }
    }

  object Autowire extends autowire.Server[Js.Value, Reader, Writer] {
    def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)
    def write[Result: Writer](r: Result) = upickle.default.writeJs(r)
  }

  object test {
    import sys.process._
    def down(urls: Iterable[String], out: String) = {
      val size =
        urls.map {url =>
          if (!new java.io.File(url.split("/").last).exists()) Seq("wget", url).!
          else 0
        }
          .takeWhile(_ == 0)
          .size
      Seq("ffmpeg", "-i", s"concat:${urls.take(size).map(_.split("/").last).mkString("|")}", "-c", "copy", s"$out.mp4").!
    }
    0 to (1 << 20) foreach { collectedAns ⇒
      val ans = Stream.iterate(collectedAns, 10)(_>>2).map(_%4).toArray
      def eq(first: Int, others: Int*) = others.forall(ans(_) == ans(first))
      val answerTry: List[List[Boolean]] = List(
        List.fill(4)(false).updated(ans(0), true), //1
        List(ans(4) == 2, ans(4) == 3, ans(4) == 0, ans(4) == 1), //2
        List((2,5,1,3), (5,2,1,3), (1,2,5,3), (3,2,5,1)) map { case (w,x,y,z) ⇒ !eq(w, x) && !eq(w, y) && !eq(w, z) }, //3
        List(eq(0, 4), eq(1, 6), eq(0, 8), eq(5, 9)), //4
        List(eq(4, 7), eq(4, 3), eq(4, 8), eq(4, 6)), //5
        List(eq(7, 1, 3), eq(7, 0, 5), eq(7, 2, 9), eq(7, 4, 8)), //6
        List.fill(4)(false).updated(ans(6), true), //7 暂时忽略
        List(6, 4, 1, 9) map (i ⇒ Math.abs(ans(i) - ans(0)) != 1), //8
        List(5, 9, 1, 8) map (eq(_, 4) ^ eq(0, 5)))
      val stats = ans.groupBy(identity).mapValues(_.length).toList.sortBy(_._2) //List[(选项, 频率)]
      if (stats.last._2 - stats.head._2 == List(3, 2, 4, 1)(ans(9)) && stats.head._1 == ans(6) //10, 7
        && answerTry.zipWithIndex.forall {case (q, i) ⇒ q.count(_ == true) == 1 && q(ans(i))})
        println(ans.map("ABCD".toList.apply).mkString(""))
    }
  }
}

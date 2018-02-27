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

}

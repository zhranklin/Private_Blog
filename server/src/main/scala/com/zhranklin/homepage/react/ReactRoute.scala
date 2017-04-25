package com.zhranklin.homepage.react

import java.util.Date

import akka.http.scaladsl.model.{ContentType, HttpEntity}
import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.Apis.ArticleApi
import com.zhranklin.homepage.Dtos.{ArticleEdit, ArticleItem}
import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.blog.Article
import org.bson.types.ObjectId
import upickle.Js
import upickle.default._

import scala.util.Try

/**
 * Created by Zhranklin on 2017/3/1.
 */
trait ReactRoute extends RouteService with ArticleApi {

  import com.zhranklin.homepage.ActorImplicits._
  import com.zhranklin.homepage.blog.db._

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
    (post & path("api" / Segments) & extract(_.request.entity match {
      case HttpEntity.Strict(nb: ContentType.NonBinary, data) =>
        data.decodeString(nb.charset.value)
    })) { (s, e) ⇒
      complete {
        Autowire.route[ArticleApi](this)(
          autowire.Core.Request(
            s,
            upickle.json.read(e).asInstanceOf[Js.Obj].value.toMap
          )
        ).map(upickle.json.write(_))
      }
    }

  object Autowire extends autowire.Server[Js.Value, Reader, Writer] {
    def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)
    def write[Result: Writer](r: Result) = upickle.default.writeJs(r)
  }

  def asItem(a: Article) = ArticleItem(a.id.get.toHexString, a.title, a.author, a.create_time.dateString, a.abs, a.tags)

  def asEdit(a: Article) = ArticleEdit(a.title, a.author, a.section, a.mdown, a.html, a.abs, a.tags)

  def fromEdit(id: Option[ObjectId], a: ArticleEdit) = Article(a.title, a.author, a.section, a.mdown, a.html, a.abs, a.tags, new Date, new Date, id)

  def list() = articleList(None).map(asItem)

  def get(id: String): Option[ArticleEdit] = for {
    bid ← Try(new ObjectId(id)).toOption //验证id这个字符串是否符合ObjectId构造器的要求,如果不符合则可认为是新文章
    mongo ← articles.findOneByID(bid)
  } yield asEdit(new Article(mongo))

  def save(id: Option[String], article: ArticleEdit) = {
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

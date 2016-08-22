package com.zhranklin.homepage

import java.net.URLEncoder.encode
import java.net.URLDecoder.decode
import java.util.Date

import akka.actor.Actor
import akka.io.IO
import akka.pattern.ask
import com.mongodb.casbah.Imports.{MongoDBList => $$, MongoDBObject => $, _}
import com.zhranklin.homepage.blog.Article
import com.zhranklin.homepage.blog.db._
import org.bson.types.ObjectId
import spray.can.Http
import spray.client.pipelining.{SendReceive, _}
import spray.http._
import spray.httpx.Json4sJacksonSupport
import spray.httpx.PlayTwirlSupport._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Try

class MyServiceActor extends Actor with MyService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}

case class SolrDoc(tstamp: String, title: String, url: String, content: String)
case class SolrQueryResponse(docs: List[SolrDoc])
case class SolrQueryResult(response: SolrQueryResponse)

trait JsonSupport extends Json4sJacksonSupport {
  import org.json4s.DefaultFormats
  override implicit def json4sJacksonFormats = DefaultFormats
}

trait MyService extends HttpService with JsonSupport {
  import Boot._

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
    path("blog" / Rest) {s =>
      complete {
        val str = s.replaceAll("\\+", "%2B")
        html.article.render(articles.findOne($("title" -> decode(str, "UTF-8"))).get)
      }
    } ~
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
        val ar: Option[Article] =
          if (title == "") None
          else {
            val t = title.replaceAll("\\+", "%2B")
            for {
              article <- articles.findOne($("title" -> java.net.URLDecoder.decode(t, "UTF-8")))
              md <- article.mdown
            } yield article
          }
        if (ar.isEmpty && title != "")
          html.message.render("Error", "该文章不存在或无法编辑.")
        else
          html.editor.render(ar)
      }
    } ~ solrRoute

  val solrHost = "solr.zhranklin.com"
  val solrPort = 80
  import ContentTypes._
  def changeContentType(response: HttpResponse): HttpResponse =
    response.mapEntity(entity ⇒ HttpEntity.NonEmpty(`application/json`, entity.toOption.get.data))
  val pipeline: HttpRequest ⇒ Future[SolrQueryResult] =
    sendReceive ~> changeContentType ~> unmarshal[SolrQueryResult]
  lazy val solrRoute =
    path("solr" / Rest) { rest ⇒
      complete {
        val pipeline: Future[HttpRequest ⇒ Future[SolrQueryResult]] = (for (
          Http.HostConnectorInfo(connector, _) <- IO(Http) ? Http.HostConnectorSetup(solrHost, port = solrPort)
        ) yield sendReceive(connector)).map(_ ~> changeContentType ~> unmarshal[SolrQueryResult])
        import concurrent.duration._
        val queryUrl = s"/solr/select?q=${encode(rest, "UTF-8")}&wt=json"
        val resultFuture: Future[SolrQueryResult] = pipeline.flatMap(_(Get(queryUrl)))
        val result = Await.result(resultFuture, 5.seconds)
        val doc:SolrDoc = result.response.docs(1)
        html.message.render(doc.title, doc.content)
      }
    }
}

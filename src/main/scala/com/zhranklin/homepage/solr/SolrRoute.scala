package com.zhranklin.homepage.solr


import java.lang.Integer.parseInt
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Date

import com.zhranklin.homepage.Boot._
import com.zhranklin.homepage.{JsonSupport, MyHttpService, PageItem}
import spray.client.pipelining._
import spray.http.ContentTypes.`application/json`
import spray.http._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Try

case class SolrDoc(tstamp: String, title: String, url: String, content: String) extends PageItem {
  override val itemTitle: String = title
  override val itemText: String =
    if (content.length <= 200) content
    else content.take(200)
  override val itemLink: String = url
  override val itemTags: List[String] = Nil
  override val itemTime: Date = {
    val stampPattern = "(\\d+)-(\\d+)-(\\d+)T(\\d+):(\\d+):(\\d+)\\.(\\d+)Z".r
    //lazy for preventing exception
    lazy val List(yy, mm, dd, h, m, s, _) = stampPattern.findFirstMatchIn(tstamp).get.subgroups.map(parseInt)
    lazy val stamp = Timestamp.valueOf(LocalDateTime of(yy, mm, dd, h, m, s))
    Try(stamp).getOrElse(new Date)
  }
}
case class SolrQueryResponse(docs: List[SolrDoc])
case class SolrQueryResult(response: SolrQueryResponse)

trait SolrRoute extends MyHttpService with JsonSupport {
  def changeContentType(response: HttpResponse): HttpResponse =
    response.mapEntity(entity ⇒ HttpEntity.NonEmpty(`application/json`, entity.toOption.get.data))

  def enc = java.net.URLEncoder.encode(_: String, "UTF-8")
  def dec = java.net.URLDecoder.decode(_: String, "UTF-8")

  val env = scala.sys.env
  val solrHost = env.getOrElse("SOLR_HOST", "127.0.0.1")
  val solrPort = env get "SOLR_PORT" map Integer.parseInt getOrElse 8983

  val pipeline = sendReceive ~> changeContentType ~> unmarshal[SolrQueryResult]

  val solrRoute =
    path("solr") {
      parameter('keyword) { keyword ⇒
        complete {
          val url = s"http://$solrHost:$solrPort/solr/select?q=${enc(keyword)}&wt=json"
          val request = Get(url)
          def resultFuture = pipeline(request)
          def gettingResult: SolrQueryResult = Await.result(resultFuture, 5.seconds)
          val docsOpt = for {
            result ← Try(gettingResult)
          } yield result.response.docs
          resultFuture.map{
            res: SolrQueryResult ⇒ html.index.render(s"Result of: $keyword", "search", res.response.docs)
          }.recover{case _ ⇒ html.message.render("Info", s"No results for search: $keyword")}
        }
      } ~
      complete {
        html.solr.render()
      }
    }
}

package com.zhranklin.blog
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject => $, MongoDBList => $$}
import java.util.Date
import java.text.DateFormat

case class Article(title: String, author: String,
                   mdown: Option[String] = None, html: String = "",
                   abs: String, tags: Seq[String] = Nil,
                   create_time: Date = new Date(), edit_time: Date = new Date ()) {

  def this(md: DBObject) = this(
    md.getAs[String]("title").get,
    md.getAs[String]("author").get,
    md.getAs[String]("markdown"),
    md.getAs[String]("content").get,
    md.getAs[String]("abstract").get,
    for (m <- md getAs[$$] "tags" getOrElse Nil) yield m.asInstanceOf[String],
    md.getAs[Date]("create_time").get,
    md.getAs[Date]("edit_time").get)

  def modify(t: String = title, au: String = author,
             md: Option[String] = mdown, c: String = html, ab: String = abs,
             ct: Date = create_time, et: Date = new Date, ta: Seq[String] = tags) =
             Article(t, au, html = c, abs = ab, tags = ta, create_time = ct, edit_time = et)

  def mongo = $( "title"       -> title,
                 "author"      -> author,
                 "markdown"    -> mdown,
                 "content"     -> html,
                 "abstract"    -> abs,
                 "create_time" -> create_time,
                 "edit_time"   -> edit_time,
                 "tags"        -> tags)
}

object db {

  implicit val articles = MongoClient()("test")("articles")

  def articleList = articleList_var
  private var articleList_var = getArticleList
  private def getArticleList = articles.find.toList map (new Article(_)) sortWith ((a1, a2) => a1.create_time after a2.create_time)
  def refreshArticleList() = articleList_var = getArticleList

  private val df = DateFormat.getDateInstance(DateFormat.LONG)

  implicit def mongoDBObjectToArticle(m: DBObject):Article = new Article(m)
  implicit class printableDate(d: Date) { def dateString = df.format(d) }

  /* searchArticle split the keywords by space and search the articles that match them for the fields
  * e.g. searchArticle(List("title", "content"), "a b") => (title~a or content~a) and (title~b or content~b)*/
  def searchArticle(fields: List[String], keywords: String)(implicit c: MongoCollection): Seq[Article] = {
    val filter = $and(
      keywords
      split " "
      map { w ⇒
        val pattern = w.r
        $or(fields map {_ $eq pattern}: _*)
      }: _*
    )
    c.find(filter).toList map mongoDBObjectToArticle
  }

}

package com.zhranklin.homepage.blog

import java.text.DateFormat
import java.util.Date

import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.PageItem
import org.bson.types.ObjectId

case class Article(title: String, author: String, section: String,
                   mdown: Option[String] = None, html: String = "",
                   abs: String, tags: List[String] = Nil,
                   create_time: Date = new Date(), edit_time: Date = new Date (),
                   id: Option[ObjectId] = None) extends PageItem {

  override val itemTitle = title
  override val itemLink = s"/blog/$title"
  override val itemTags = tags
  override val itemText = html.replaceAll("<.*?>", " ").replaceAll("\\s\\s+", " ").take(200)
  override val itemTime: Date = create_time

  def this(md: DBObject) = this(
    md.getAs[String]("title").get,
    md.getAs[String]("author").get,
    md.getAs[String]("section").getOrElse("tech"),
    md.getAs[String]("markdown"),
    md.getAs[String]("content").get,
    md.getAs[String]("abstract").get,
    (for (m <- md getAs[$$] "tags" getOrElse Nil) yield m.asInstanceOf[String]).toList,
    md.getAs[Date]("create_time").get,
    md.getAs[Date]("edit_time").get,
    md.getAs[ObjectId]("_id"))

  def modify(t: String = title, au: String = author, sec: String,
             md: Option[String] = mdown, c: String = html, ab: String = abs,
             ct: Date = create_time, et: Date = new Date, ta: List[String] = tags) =
    Article(t, au, section = sec, html = c, abs = ab, tags = ta, create_time = ct, edit_time = et)

  def mongo = $( "title" -> title, "author" -> author, "section" → section,
                 "markdown" -> mdown, "content" -> html,
                 "abstract" -> abs, "tags" -> tags,
                 "create_time" -> create_time, "edit_time" -> edit_time)

}

object db {

  implicit val articles = MongoClient()("test")("articles")
  implicit def mongoDBObjectToArticle(m: DBObject):Article = new Article(m)
  implicit class printableDate(d: Date) { def dateString = df.format(d) }

  def articleList(secOpt: Option[String]) =
    secOpt map (s ⇒ articleList_var.filter(_.section == s)) getOrElse
      articleList_var.filter(a ⇒ List("tech", "misc", "music").contains(a.section))
  def refreshArticleList() = articleList_var = getArticleList

  private var articleList_var = getArticleList
  private def getArticleList =
    articles.find.toList
      .map(new Article(_))
      .sortWith((a1, a2) => a1.create_time after a2.create_time)
  private val df = DateFormat.getDateInstance(DateFormat.LONG)

  /* searchArticle split the keywords by space and search the articles that match them for the fields
  * e.g. searchArticle(List("title", "content"), "a b") => (title~a or content~a) and (title~b or content~b)*/
  def searchArticle(fields: List[String], keywords: String)(implicit c: MongoCollection): Seq[Article] = {
    val filter = $and(
      keywords split " "
      filterNot (_ matches "\\s*")
      map { w ⇒
        val pattern = w.r
        $or(fields map {_ $eq pattern}: _*)
      }: _*
    )
    c.find(filter).toList map mongoDBObjectToArticle
  }
}

object util {
  implicit class StringToStringLiteral(string: String) {
    def toStringLiteral = "\"" + string
        .replaceAll("\\\\", "\\\\\\\\")
        .replaceAll("\"", "\\\\\"")
        .lines
        .mkString("\\n\" + \"") + "\""
  }
}
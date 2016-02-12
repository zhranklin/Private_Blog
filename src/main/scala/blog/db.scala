package com.zhranklin.blog
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject => $, MongoDBList => $$}
import java.util.Date
import java.text.DateFormat

case class Article(title: String, author: String, content: String, abs: String,
                   create_time: Date = new Date(), edit_time: Date = new Date (),
                   tags: Seq[String] = Nil){
  def this(md: DBObject) = this(
    md.getAs[String]("title").get,
    md.getAs[String]("author").get,
    md.getAs[String]("content").get,
    md.getAs[String]("abstract").get,
    md.getAs[Date]("create_time").get,
    md.getAs[Date]("edit_time").get,
    for (m <- md getAs[$$] "tags" getOrElse Nil) yield m.asInstanceOf[String])
  def modify(t: String = title, au: String = author, c: String = content, ab: String = abs,
             ct: Date = create_time, et: Date = new Date, ta: Seq[String] = tags) =
             Article(t, au, c, ab, ct, et, ta)
  def mongo = $("title" -> title,
                 "author" -> author,
                 "content" -> content,
                 "abstract" -> abs,
                 "create_time" -> create_time,
                 "edit_time" -> edit_time,
                 "tags" -> tags)
}

object db {
  implicit def mongoDBObjectToArticle(m: DBObject):Article = new Article(m)
  implicit class printableDate(d: Date) { def dateString = df.format(d) }
  val coll = MongoClient()("test")("bulkOperation")
  val articles = MongoClient()("test")("articles")
  private val df = DateFormat.getDateInstance(DateFormat.LONG)
  val articlelist = articles.find.toList map (new Article(_))
}

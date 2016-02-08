package com.zhranklin.blog
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject => $$}
import java.util.Date

case class Article(title: String, author: String, content: String, abs: String,
                   create_time: Date = new Date(), edit_time: Date = new Date ()){
  def this(md: DBObject) = this(md.get("title").asInstanceOf[String],
                                     md.get("author").asInstanceOf[String],
                                     md.get("content").asInstanceOf[String],
                                     md.get("abstract").asInstanceOf[String],
                                     md.get("create_time").asInstanceOf[Date],
                                     md.get("edit_time").asInstanceOf[Date])
  def mongo = $$("title" -> title,
                 "author" -> author,
                 "content" -> content,
                 "abstract" -> abs,
                 "create_time" -> create_time,
                 "edit_time" -> edit_time)
}

object db {
  val coll = MongoClient()("test")("bulkOperation")
  val articles = MongoClient()("test")("articles")
}

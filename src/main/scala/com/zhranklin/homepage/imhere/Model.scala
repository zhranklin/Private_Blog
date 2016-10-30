package com.zhranklin.homepage.imhere

import com.zhranklin.homepage.HaveOID
import org.bson.types.ObjectId

/**
  * Created by Zhranklin on 16/10/5.
  */
object Model {


  trait CaseWithNames { self: Product ⇒
    def names: Array[String]
    def getMap: Seq[(String, Any)] =
      names.zipWithIndex map (tp ⇒ tp._1 → productElement(tp._2)) filter (tp ⇒ tp._2 != None && tp._2 != null)
  }

  case class Place(uuid: String, name: String) extends CaseWithNames {
    def names = Array("uuid", "name")
  }
  case class Item(title: String, `type`: String, content: String, place: String, id: ObjectId = null) extends CaseWithNames with HaveOID {
    def names = Array("title", "type", "content", "place", "_id")
  }
  object Item {
    val is = List(
      Item("title0", "text", "kkk", "001"),
      Item("title1", "html", "<h1>head</h1><p>ttext</p>", "001"),
      Item("title2", "html", "<h2>head2</h2><p>text</p>", "002"),
      Item("title3", "url", "http://www.baidu.com", "002"),
      Item("title4", "text", "kkk4", "001"),
      Item("title5", "text", "kkk5", "002"),
      Item("title6", "text", "kkk6", "001"),
      Item("title7", "text", "kkk7", "002"),
      Item("title8", "text", "kkk8", "001"),
      Item("title9", "text", "kkk9", "002"))
  }
}

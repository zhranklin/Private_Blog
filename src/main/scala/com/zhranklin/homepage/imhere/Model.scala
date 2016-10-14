package com.zhranklin.homepage.imhere

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
  case class Item(title: String, `type`: String, content: String, place: String, id: ObjectId = null) extends CaseWithNames {
    def names = Array("title", "type", "content", "place", "_id")
  }
}

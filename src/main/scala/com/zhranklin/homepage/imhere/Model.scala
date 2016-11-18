package com.zhranklin.homepage.imhere

import com.zhranklin.homepage._
import org.bson.types.ObjectId

/**
  * Created by Zhranklin on 16/10/5.
  */
object Model {

  import Util._idRename

  case class Place(id: String, name: String) extends _idRename
  case class Item(title: String, `type`: String, content: String,
                  place: String, owner: String, id: Option[ObjectId] = None) extends _idRename {
    def withId(id: ObjectId) = Item(title, `type`, content, place, owner, Some(id))
  }
  case class User(username: String, name: String)
  case class UserPass(username: String, name: String, password: String) {
    def asUser = User(username, name)
  }
  object Item1 {
    val is = List(
      Item("title0", "text", "kkk", "001", "public"),
      Item("title1", "html", "<h1>head</h1><p>ttext</p>", "001", "public"),
      Item("title2", "html", "<h2>head2</h2><p>text</p>", "002", "public"),
      Item("title3", "url", "http://www.baidu.com", "002", "public"),
      Item("title4", "text", "kkk4", "001", "public"),
      Item("title5", "text", "kkk5", "002", "public"),
      Item("title6", "text", "kkk6", "001", "public"),
      Item("title7", "text", "kkk7", "002", "public"),
      Item("title8", "text", "kkk8", "001", "public"),
      Item("title9", "text", "kkk9", "002", "public"))
  }
}

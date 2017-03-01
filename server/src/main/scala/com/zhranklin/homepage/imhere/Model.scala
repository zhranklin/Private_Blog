package com.zhranklin.homepage.imhere

import com.zhranklin.homepage._
import org.bson.types.ObjectId

/**
  * Created by Zhranklin on 16/10/5.
  */
object Model {

  import Util._idRename

  case class Place(id: String, name: String) extends _idRename
  case class ItemWithOwner(title: String, `type`: String, content: String,
                           place: String, owner: String, id: Option[ObjectId] = None) extends _idRename {
    def withId(id: ObjectId) = ItemWithOwner(title, `type`, content, place, owner, Some(id))
    def asItem = Item(title, `type`, content, place, id)
  }
  case class Item(title: String, `type`: String, content: String,
                  place: String, id: Option[ObjectId] = None) extends _idRename {
    def withId(id: ObjectId) = Item(title, `type`, content, place, Some(id))
    def withOwner(owner: String) = ItemWithOwner(title, `type`, content, place, owner, id)
  }
  case class User(username: String, name: String)
  case class UserPass(username: String, name: String, password: String) {
    def asUser = User(username, name)
  }
  object Item1 {
    val is = List(
      ItemWithOwner("title0", "text", "kkk", "001", "public"),
      ItemWithOwner("title1", "html", "<h1>head</h1><p>ttext</p>", "001", "public"),
      ItemWithOwner("title2", "html", "<h2>head2</h2><p>text</p>", "002", "public"),
      ItemWithOwner("title3", "url", "http://www.baidu.com", "002", "public"),
      ItemWithOwner("title4", "text", "kkk4", "001", "public"),
      ItemWithOwner("title5", "text", "kkk5", "002", "public"),
      ItemWithOwner("title6", "text", "kkk6", "001", "public"),
      ItemWithOwner("title7", "text", "kkk7", "002", "public"),
      ItemWithOwner("title8", "text", "kkk8", "001", "public"),
      ItemWithOwner("title9", "text", "kkk9", "002", "public"))
  }
}

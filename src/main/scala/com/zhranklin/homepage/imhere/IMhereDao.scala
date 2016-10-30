package com.zhranklin.homepage.imhere

import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.imhere.Model._

import scala.util.Try

object PlaceDao {
  private val place = MongoClient()("imhere")("place")

  private def mongoToPlace(m: DBObject) = Place(m.getAs[String]("uuid").get, m.getAs[String]("name").get)
  def get(uuid: String): Place = place.findOne($("uuid" → uuid)).map(mongoToPlace).get
  def add(p: Place): Unit = place.insert($(p.getMap: _*))
  def delete(uuid: String) = place.remove($("uuid" → uuid)).getN > 0
  def update(uuid: String, p: Place) = place.update($("uuid" → uuid), $(p.getMap:_*)).isUpdateOfExisting
}

object ItemDao {
  private val item = MongoClient()("imhere")("item")

  private def mongoToItem(m: DBObject) = Item(
    m.getAs[String]("title").get,
    m.getAs[String]("type").get,
    m.getAs[String]("content").get,
    m.getAs[String]("place").get,
    m.getAs[ObjectId]("_id").get)

  def idTuple(id: String) = "_id" → new ObjectId(id)

  def get(id: String): Item = item.findOne($(idTuple(id))).map(mongoToItem).get
  def add(i: Item): Unit = Try(item.insert($(i.getMap:_*))).toOption.map(r ⇒ i)
  def delete(id: String) = item.remove($(idTuple(id))).getN > 0
  def update(id: String, i: Item) = item.update($(idTuple(id)), $(i.getMap:_*)).isUpdateOfExisting
}

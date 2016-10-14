package com.zhranklin.homepage.imhere

import com.zhranklin.homepage.imhere.Model._
import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}

import scala.util.Try

object PlaceDao {
  private val place = MongoClient()("imhere")("place")

  private def mongoToPlace(m: DBObject) = Place(m.getAs[String]("uuid").get, m.getAs[String]("name").get)
  def get(uuid: String): Option[Place] = place.findOne($("uuid" → uuid)).map(mongoToPlace)
  def add(p: Place): Option[Place] = Try(place.insert($(p.getMap: _*))).toOption.map(r ⇒ p)
  def delete(uuid: String): Option[Place] = for {
    deleted ← place.findOne($("uuid" → uuid))
    _ ← Try(place.remove($("uuid" → uuid))).toOption
  } yield mongoToPlace(deleted)
  def update(uuid: String, p: Place): Option[Place] =
    Try(place.update($("uuid" → uuid), $(p.getMap:_*))).toOption.map(r ⇒ p)
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

  def get(id: String): Option[Item] = item.findOne($(idTuple(id))).map(mongoToItem)

  def add(i: Item): Option[Item] = Try(item.insert($(i.getMap:_*))).toOption.map(r ⇒ i)
  def delete(id: String): Option[Item] = for {
    deleted ← item.findOne($(idTuple(id)))
    _ ← Try(item.remove($(idTuple(id)))).toOption
  } yield mongoToItem(deleted)
  def update(id: String, i: Item): Option[Item] =
    Try(item.update($(idTuple(id)), $(i.getMap:_*))).toOption.map(r ⇒ i)
}

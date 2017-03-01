package com.zhranklin.homepage.imhere

import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}
import com.zhranklin.homepage.JsonForMongo._
import com.zhranklin.homepage.imhere.Model._

import scala.util.Try

object PlaceDao {
  private val place = MongoClient()("imhere")("place")

//  private def mongoToPlace(m: DBObject) = Place(m.getAs[String]("uuid").get, m.getAs[String]("name").get)
  def get(id: String): Option[Place] = place.findOne($("_id" → id)).map(_.read[Place])
  def add(p: Place): Unit = place.insert(p.mongo)
  def delete(id: String) = place.remove($("_id" → id)).getN > 0
  def update(id: String, p: Place) = place.update($("_id" → id), p.mongo).isUpdateOfExisting
}

object ItemDao {
  private val item = MongoClient()("imhere")("item")

//  private def mongoToItem1(m: DBObject) = Item(
//    m.getAs[String]("title").get,
//    m.getAs[String]("type").get,
//    m.getAs[String]("content").get,
//    m.getAs[String]("place").get,
//    m.getAs[String]("owner").get,
//    m.getAs[ObjectId]("_id"))

//  private def mongoToItem(m: DBObject) = readMongo[Item](m)

  def idTuple(id: String) = "_id" → new ObjectId(id)

  def get(id: String): Option[ItemWithOwner] = item.findOne($(idTuple(id))).map(_.read[ItemWithOwner])
  def getAll(user: User):List[ItemWithOwner] = {
    val owners = Set("public") + user.username map (u ⇒ "owner" $eq u)
    item.find($or(owners.toList: _*)).toList.map(_.read[ItemWithOwner])
  }
  def add(i: ItemWithOwner): Try[ObjectId] = Try{
    val mongo = i.mongo
    item.save(mongo)
    mongo.getAs[ObjectId]("_id").get
  }
  def delete(id: String) = item.remove($(idTuple(id))).getN > 0
  def update(id: String, i: ItemWithOwner) = item.update($(idTuple(id)), i.mongo).isUpdateOfExisting
  def findByPlace(uuid: String, user: User) = {
    val owners = Set("public") + user.username map (u ⇒ "owner" $eq u)
    item.find($("place" → uuid) ++ $or(owners.toList: _*)).map(_.read[ItemWithOwner]).toList
  }
}

object UserDao {
  private val user = MongoClient()("imhere")("user")

  def unQuery(un: String) = $("username" → un)

//  private def mongoToUser(m: DBObject) = User(
//    m.getAs[String]("username").get,
//    m.getAs[String]("name").get
//  )

  private def get(un: String): Option[User] = user.findOne(unQuery(un)).map(_.read[User])
  def get(un: String, v: String ⇒ Boolean): Option[User] = for {
    mongo ← user.findOne(unQuery(un))
    pass ← mongo.getAs[String]("password")
    if v(pass)
  } yield mongo.read[User]
  def add(u: UserPass): Unit = Try(user.insert(u.mongo)).toOption.map(r ⇒ u)
  def delete(un: String) = user.remove(unQuery(un)).getN > 0
  def update(id: String, i: User) = user.update(unQuery(id), i.mongo).isUpdateOfExisting
}

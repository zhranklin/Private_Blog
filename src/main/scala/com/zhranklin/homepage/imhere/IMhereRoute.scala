package com.zhranklin.homepage.imhere

import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsMissing
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.Credentials
import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.imhere.Model._

trait IMhereRoute extends RouteService {
  import com.zhranklin.homepage.BasicJsonSupport._
  abstract override def myRoute = super.myRoute ~
    (pathPrefix("imh") & authenticateBasic("imh security", iMhereAuthenticator)) { user ⇒
      pathPrefix("place") {
        pathPrefix(Segment) { uuid ⇒
          (get & complete _) {
            PlaceDao.get(uuid).get
          } ~
          (delete & complete _) {
            val pl = PlaceDao.get(uuid).get
            PlaceDao.delete(uuid)
            pl
          } ~
          (put & entity(as[Place])) { pl ⇒ complete {
            PlaceDao.update(uuid, pl)
            pl
          }}
        } ~
        (pathEnd & post & entity(as[Place])) { pl ⇒
          PlaceDao.add(pl)
          complete(pl)
        }
      } ~
      pathPrefix("item") {
        pathSingleSlash {
          complete(ItemDao.getAll(user))
        } ~
        pathPrefix("[a-fA-F0-9]{24}".r) { bid ⇒
          (get & complete _) {
            ItemDao.get(bid).get
          } ~
          (delete & complete _) {
            val pl = ItemDao.get(bid).get
            ItemDao.delete(bid)
            pl
          } ~
          (put & entity(as[Item])) { item ⇒ complete {
            ItemDao.update(bid, item)
            item
          }}
        } ~
        (pathEnd & post & entity(as[Item])) { item ⇒
          val idTry = ItemDao.add(item)
          println(idTry)
          val id = idTry.get
          complete(item.withId(id))
        } ~
        pathPrefix("at" / Segment) { uuid ⇒ complete {
          ItemDao.findByPlace(uuid, user)
        }}
      } ~
      (pathPrefix("user") & pathEnd) {
        get {
          if (user.username == "public")
            reject(AuthenticationFailedRejection(CredentialsMissing, HttpChallenge("http", "imh security")))
          else
            complete(user)
        } ~
        (post & entity(as[UserPass])) { userPass ⇒ complete {
          UserDao.add(userPass)
          userPass.asUser
        }}
      }
    }
  def iMhereAuthenticator(credentials: Credentials): Option[User] = credentials match {
    case Credentials.Missing ⇒ Some(User("public", "public"))
    case p @ Credentials.Provided(username) ⇒ UserDao.get(username, p.verify)
  }
}

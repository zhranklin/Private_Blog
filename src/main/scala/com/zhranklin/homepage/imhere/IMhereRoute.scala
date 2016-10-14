package com.zhranklin.homepage.imhere

import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.JsonSupport._
import com.zhranklin.homepage.imhere.Model._

trait IMhereRoute extends RouteService {
  abstract override def myRoute = super.myRoute ~
    pathPrefix("imh") {
      pathPrefix("place") {
        pathPrefix(Rest) { uuid ⇒
          get {
            PlaceDao.get(uuid).map(pl ⇒ complete(pl))
              .getOrElse(reject)
          } ~
          delete {
            PlaceDao.delete(uuid).map(pl ⇒ complete(pl))
              .getOrElse(reject)
          } ~
          put {
            entity(as[Place]) { pl ⇒
              PlaceDao.update(uuid, pl).map(p ⇒ complete(p))
                .getOrElse(reject)
            }
          }
        } ~
        post {
          entity(as[Place]) { pl ⇒
            PlaceDao.add(pl).map(p ⇒ complete(p))
              .getOrElse(reject)
          }
        }
      }
    }
}

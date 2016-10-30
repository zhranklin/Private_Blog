package com.zhranklin.homepage.imhere

import com.zhranklin.homepage.RouteService
import com.zhranklin.homepage.imhere.Model._

trait IMhereRoute extends RouteService {
  abstract override def myRoute = super.myRoute ~
    pathPrefix("imh") {
      pathPrefix("place") {
        pathPrefix(Segment) { uuid ⇒
          (get & complete _) {
            PlaceDao.get(uuid)
          } ~
          (delete & complete _) {
            if (PlaceDao.delete(uuid))
              PlaceDao.get(uuid)
            else
              "error"

          } ~
          (put & entity(as[Place])) { pl ⇒ ctx ⇒
            PlaceDao.update(uuid, pl)
            ctx.complete(pl)
          }
        } ~
        (post & entity(as[Place])) { pl ⇒ complete {
          PlaceDao.add(pl)
          pl
        }
        }
      } ~
      pathPrefix("item") {
        get { ctx ⇒
          ctx.complete("")
        }
      } ~
      (pathPrefix("test") & post & entity(as[String])) {str ⇒ complete(str)

      }
    }
}

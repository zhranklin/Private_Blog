package com.zhranklin.homepage.bus

import com.zhranklin.homepage.RouteService

/**
 * Created by Zhranklin on 16/12/1.
 */
trait BusRoute extends RouteService {
  abstract override def myRoute = super.myRoute
}

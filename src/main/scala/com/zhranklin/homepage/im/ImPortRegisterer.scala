package com.zhranklin.homepage.im

import java.io.{BufferedReader, InputStreamReader}
import java.net._

import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object ImPortRegisterer {
  val localPort = ConfigFactory.load().getInt("settings.socket.server_port")
  val ips = collection.mutable.Map.empty[String, String]
  import scala.concurrent.ExecutionContext.Implicits.global
  Future {
    while(true) {
      val ss = new ServerSocket(localPort)
      val socket = ss.accept()
      val (address, port) = (socket.getInetAddress.getHostName, socket.getPort)
      val rdr = new BufferedReader(new InputStreamReader(socket.getInputStream))
      println("begin readline")
      val result = rdr.readLine()
      ips += (result → s"$address:$port")
      println(s"add ip: $address:$port")
      rdr.close()
      socket.close()
      ss.close()
    }
  }
}

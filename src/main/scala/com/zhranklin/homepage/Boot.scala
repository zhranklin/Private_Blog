package com.zhranklin.homepage

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl._

import akka.http.scaladsl._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.io.{Source, StdIn}

object Boot extends App with MyRouteService {

  import ActorImplicits._

  implicit val timeout = Timeout(5.seconds)

  Class.forName("com.zhranklin.homepage.scalikejdbc.SqlTest$")

  val conf = ConfigFactory.load().getConfig("settings.server")
  val httpBindingFuture = Http().bindAndHandle(myRoute, "localhost", conf.getInt("http_port"))
  val httpsBindingFuture =
    Http().bindAndHandle(myRoute, "localhost", conf.getInt("https_port"), SSLConfig.https)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  Seq(httpBindingFuture, httpsBindingFuture).foreach { _
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())
  }
}

object SSLConfig {
  val https: HttpsConnectionContext = {

    val password: Array[Char] = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("password")).toArray.filter(_ != '\n')

    val ks: KeyStore = KeyStore.getInstance("jks")
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("zhranklin.com.jks")

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    ConnectionContext.https(sslContext)
  }

}
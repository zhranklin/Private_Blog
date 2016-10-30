package com.zhranklin.homepage.notice

trait DateUtils {
  import scala.collection.JavaConverters._
  val du = new {
    val splitters = List("-", "\\.", "/")
    val splittersOr = splitters mkString "|"
    val d2 = "\\d{1,2}"
    val dPattern = String.join("|", splitters.map(s ⇒ s"(?:(?:\\d{4}$s)?$d2$s$d2(?:\\s+$d2:$d2(?::$d2)?)?)").asJava)
    val dMatch = s".*?(?<!\\d)($dPattern)(?!\\d).*?".r

  }
}

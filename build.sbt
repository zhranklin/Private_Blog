organization  := "com.zhranklin.blog"

version       := "0.1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Yopt:_")

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val akka_httpV = "3.0.0-RC1"
  Seq(
//    "org.scala-lang"      %   "scala-reflect"       % "2.11.8",
    "com.typesafe.akka"   %%  "akka-http"           % akka_httpV,
//    "com.typesafe.akka"   %%  "akka-http-testkit"   % akka_httpV  % "test",
//    "com.typesafe.akka"   %%  "akka-actor"          % akkaV,
//    "com.typesafe.akka"   %%  "akka-testkit"        % akkaV       % "test",
    "com.typesafe.play"   %%  "twirl-api"           % "1.2.+",
    "de.heikoseeberger"   %%  "akka-http-json4s"    % "1.10.+",
    "org.json4s"          %%  "json4s-jackson"      % "3.4.+",
    "org.scalatest"       %%  "scalatest"           % "3.0.+"     % "test",
    "org.jsoup"           %   "jsoup"               % "1.9.2",
    "org.mongodb"         %%  "casbah"              % "3.1.+",
    "org.slf4j"           %   "slf4j-simple"        % "1.7.21",
    "org.scalikejdbc"     %%  "scalikejdbc"         % "2.4.+",
    "com.h2database"      %   "h2"                  % "1.4.+"
  )
}

Revolver.settings
lazy val root = (project in file(".")).enablePlugins(SbtTwirl)
mainClass in assembly := Some("com.zhranklin.homepage.Boot")

assemblyMergeStrategy in assembly := {
  case PathList("akka", "http", xs @ _*)         => MergeStrategy.first
  case x ⇒
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

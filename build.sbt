organization  := "com.zhranklin.blog"

version       := "0.1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Yopt:_")

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "org.scala-lang"      %   "scala-reflect"  % "2.11.8",
    "io.spray"            %%  "spray-can"      % sprayV,
    "io.spray"            %%  "spray-routing"  % sprayV,
    "io.spray"            %%  "spray-client"   % sprayV,
    "io.spray"            %%  "spray-testkit"  % sprayV   % "test",
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV    % "test",
    "org.json4s"          %% "json4s-jackson"  % "3.4.0",
    "org.scalatest"       %%  "scalatest"      % "3.0.0"  % "test",
    "org.jsoup"           %   "jsoup"          % "1.9.2",
    "org.mongodb"         %%  "casbah"         % "3.1.0",
    "org.slf4j"           %   "slf4j-simple"   % "1.7.21"
  )
}

Revolver.settings
lazy val root = (project in file(".")).enablePlugins(SbtTwirl)
mainClass in assembly := Some("com.zhranklin.homepage.Boot")


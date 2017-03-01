organization  := "com.zhranklin.blog"
version       := "0.1"

val scalaV = "2.12.1"
val akka_httpV = "10.0.4"
lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
  libraryDependencies ++= Seq(
    "com.typesafe.akka"   %%  "akka-http"           % akka_httpV,
    //    "com.typesafe.akka"   %%  "akka-http-testkit"   % akka_httpV  % "test",
    //    "com.typesafe.akka"   %%  "akka-actor"          % akkaV,
    //    "com.typesafe.akka"   %%  "akka-testkit"        % akkaV       % "test",
    "com.typesafe.play"   %%  "twirl-api"           % "1.3.0",
    "de.heikoseeberger"   %%  "akka-http-json4s"    % "1.12.0",
    "org.json4s"          %%  "json4s-jackson"      % "3.5.0",
    "org.scalatest"       %%  "scalatest"           % "3.0.1"     % "test",
    "org.jsoup"           %   "jsoup"               % "1.9.2",
    "org.mongodb"         %%  "casbah"              % "3.1.1",
    "org.slf4j"           %   "slf4j-simple"        % "1.7.21",
    "org.scalikejdbc"     %%  "scalikejdbc"         % "3.0.0-RC2",
    "com.h2database"      %   "h2"                  % "1.4.193",
    "org.apache.httpcomponents" % "httpclient"      % "4.5.2",
    "com.vmunier"         %%  "scalajs-scripts"     % "1.1.0"
  ),
  WebKeys.packagePrefix in Assets := "public/",
  managedClasspath in Runtime += (packageBin in Assets).value
).enablePlugins(SbtWeb, SbtTwirl, JavaAppPackaging).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

Revolver.settings
mainClass in assembly := Some("com.zhranklin.homepage.Boot")
assemblyMergeStrategy in assembly := {
  case PathList("akka", "http", xs @ _*)         => MergeStrategy.first
  case x ⇒
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

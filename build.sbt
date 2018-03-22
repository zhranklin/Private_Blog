val v = new {
  val scala = "2.12.4"
  val akka_http = "10.0.4"
  val reactScala = "1.2.0"
  val bs = "4.0.0-alpha.6-1"
  val autowire = "0.2.6"
  val upickle = "0.4.4"
}

organization  := "com.zhranklin.blog"
version       := "0.1"
scalaVersion in ThisBuild := v.scala

lazy val server = (project in file("server")).settings(
  scalaVersion := v.scala,
  scalaJSProjects := Seq(client),
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  Assets / pipelineStages:= Seq(scalaJSPipeline),
  // triggers scalaJSPipeline when using compile or continuous compilation
  Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++=
    "com.typesafe.akka" %% "akka-http"        % v.akka_http ::
    "com.typesafe.play" %% "twirl-api"        % "1.3.13"     ::
    "de.heikoseeberger" %% "akka-http-json4s" % "1.12.0"    ::
    "org.json4s"        %% "json4s-jackson"   % "3.5.0"     ::
    "org.scalatest"     %% "scalatest"        % "3.0.1"     %  "test"     ::
    "org.jsoup"         %  "jsoup"            % "1.9.2"     ::
    "org.mongodb"       %% "casbah"           % "3.1.1"     ::
    "org.slf4j"         %  "slf4j-simple"     % "1.7.21"    ::
    "org.apache.httpcomponents" % "httpclient"% "4.5.2"     ::
    "com.vmunier"       %% "scalajs-scripts"  % "1.1.0"     ::
    "com.zhranklin"     %% "scala-tricks"     % "0.1.0"     ::
    Nil,
  Assets / WebKeys.packagePrefix := "public/",
  npmAssets ++= NpmAssets.ofProject(client) { modules =>
    (modules / "bootstrap" / "dist" / "css") ** "*" +++
    modules / "highlightjs" / "styles" / "tomorrow.css"
  }.value,
  Runtime / managedClasspath  += (packageBin in Assets).value
).enablePlugins(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := v.scala,
  scalaJSUseMainModuleInitializer := true,
  emitSourceMaps := true,
  webpackBundlingMode := BundlingMode.LibraryOnly(),
  libraryDependencies ++=
    "org.scala-js"             %%% "scalajs-dom"    % "0.9.5"      ::
    "be.doeraene"              %%% "scalajs-jquery" % "0.9.3"      ::
    "com.github.japgolly.scalajs-react" %%% "core"  % v.reactScala ::
    "com.github.japgolly.scalajs-react" %%% "extra" % v.reactScala ::
    Nil,
  npmDependencies in Compile ++= Seq(
    "react" → "16.2.0",
    "react-dom" → "16.2.0",
    "echarts" → "4.0.4",
    "simplemde" → "1.11.2",
    "jquery" → "2.1.3",
    "jquery-ui" → "1.12.1",
    "highlightjs" → "9.10.0",
    "bootstrap" → "4.0.0",
    "tether" → "1.4.3"
  )
).enablePlugins(ScalaJSBundlerPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := v.scala,
    libraryDependencies ++=
      "org.scalaz"        %%% "scalaz-core"      % "7.2.10"    ::
      "com.lihaoyi"       %%% "autowire"         % v.autowire  ::
      "com.lihaoyi"       %%% "upickle"          % v.upickle   ::
      Nil
  )

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

//assemblyMergeStrategy in assembly := {
//  case PathList("akka", "http", xs @ _*)         => MergeStrategy.first
//  case x ⇒
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)
//}
// loads the server project at sbt startup
Global / onLoad := (Global / onLoad).value andThen {s: State => "project server" :: s}


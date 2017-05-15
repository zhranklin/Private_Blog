organization  := "com.zhranklin.blog"
version       := "0.1"
scalaVersion in ThisBuild := "2.12.2"
enablePlugins(WorkbenchPlugin)

val v = new {
  val scala = "2.12.2"
  val akka_http = "10.0.4"
  val reactScala = "1.0.0"
  val bs = "4.0.0-alpha.6-1"
  val autowire = "0.2.6"
  val upickle = "0.4.4"
}

val jsDeps = new {
  val highlightjs = "org.webjars" % "highlightjs" % "9.8.0"
  val simplemde = "org.webjars.bower" % "simplemde-markdown-editor" % "1.11.2"
  val reactJs = "org.webjars.bower" % "react" % "15.5.4"
}

lazy val server = (project in file("server")).settings(
  scalaVersion := v.scala,
  scalaJSProjects := Seq(client),
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
  libraryDependencies ++=
    "com.typesafe.akka" %% "akka-http"        % v.akka_http ::
    "com.typesafe.play" %% "twirl-api"        % "1.3.0"     ::
    "de.heikoseeberger" %% "akka-http-json4s" % "1.12.0"    ::
    "org.json4s"        %% "json4s-jackson"   % "3.5.0"     ::
    "org.scalatest"     %% "scalatest"        % "3.0.1"     %  "test"     ::
    "org.jsoup"         %  "jsoup"            % "1.9.2"     ::
    "org.mongodb"       %% "casbah"           % "3.1.1"     ::
    "org.slf4j"         %  "slf4j-simple"     % "1.7.21"    ::
    "org.apache.httpcomponents" % "httpclient"% "4.5.2"     ::
    "com.vmunier"       %% "scalajs-scripts"  % "1.1.0"     ::
    "com.zhranklin"     %% "scala-tricks"     % "0.1.0"     ::
    jsDeps.highlightjs  %  "provided"         ::
    jsDeps.simplemde    %  "provided"         ::
    Nil,
  WebKeys.packagePrefix in Assets := "public/",
  managedClasspath in Runtime += (packageBin in Assets).value
).enablePlugins(SbtWeb, SbtTwirl, JavaAppPackaging).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := v.scala,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++=
    "org.scala-js"             %%% "scalajs-dom"    % "0.9.1"      ::
    "be.doeraene"              %%% "scalajs-jquery" % "0.9.1"      ::
    "com.github.japgolly.scalajs-react" %%% "core"  % v.reactScala ::
    "com.github.japgolly.scalajs-react" %%% "extra" % v.reactScala ::
    Nil,
  jsDependencies ++= Seq("java", "scala", "vim").map(name ⇒
    jsDeps.highlightjs / s"$name.min.js" dependsOn "highlight.min.js"),
  jsDependencies ++=
    ("org.webjars" % "marked"    % "0.3.2"  / "marked.js")   ::
    ("org.webjars" % "tether"    % "1.4.0"  / "tether.min.js")   ::
    ("org.webjars" % "jquery"    % "2.1.3"  / "jquery.min.js")        ::
    ("org.webjars" % "bootstrap" % v.bs     / "bootstrap.min.js" dependsOn ("jquery.min.js", "tether.min.js")) ::
    ("org.webjars" % "jquery-ui" % "1.12.1" / "jquery-ui.min.js" dependsOn "jquery.min.js")        ::
    (jsDeps.highlightjs /   "highlight.min.js")        ::
    (jsDeps.simplemde   /   "simplemde.min.js")        ::
    (jsDeps.reactJs     /   "react-with-addons.min.js")         ::
    (jsDeps.reactJs     /   "react-dom.min.js"         dependsOn "react-with-addons.min.js") ::
    (jsDeps.reactJs     /   "react-dom-server.min.js"  dependsOn "react-dom.min.js")         ::
    Nil
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := v.scala,
    libraryDependencies ++=
      "org.scalaz"        %%% "scalaz-core"      % "7.2.10"    ::
      "com.lihaoyi"       %%% "autowire"         % v.autowire  ::
      "com.lihaoyi"       %%% "upickle"          % v.upickle   ::
      Nil
  ).
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

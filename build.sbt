organization  := "com.zhranklin.blog"
version       := "0.1"

val v = new {
  val scala = "2.12.1"
  val akka_http = "10.0.4"
  val react = "15.4.2"
  val reactScala = "1.0.0-RC2"
  val bs = "4.0.0-alpha.6-1"
  val autowire = "0.2.6"
  val upickle = "0.4.4"
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
    "org.scalatest"     %% "scalatest"        % "3.0.1"     %  "test" ::
    "org.jsoup"         %  "jsoup"            % "1.9.2"     ::
    "org.mongodb"       %% "casbah"           % "3.1.1"     ::
    "org.slf4j"         %  "slf4j-simple"     % "1.7.21"    ::
    "org.scalikejdbc"   %% "scalikejdbc"      % "3.0.0-RC2" ::
    "com.h2database"    %  "h2"               % "1.4.193"   ::
    "org.apache.httpcomponents" % "httpclient"% "4.5.2"     ::
    "com.vmunier"       %% "scalajs-scripts"  % "1.1.0"     ::
    "com.lihaoyi"       %% "autowire"         % v.autowire  ::
    "com.lihaoyi"       %% "upickle"          % v.upickle   ::
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
    "com.lihaoyi"              %%% "autowire"       % v.autowire   ::
    "com.lihaoyi"              %%% "upickle"        % v.upickle    ::
    "com.lihaoyi"              %%% "scalarx"        % "0.3.2"      ::
    "com.github.japgolly.scalajs-react" %%% "core"  % v.reactScala ::
    "com.github.japgolly.scalajs-react" %%% "extra" % v.reactScala ::
    Nil,
  // React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
  jsDependencies ++=
    ("org.webjars" % "marked"      % "0.3.2" / "marked.js") ::
    ("org.webjars" % "tether"      % "1.4.0" / "tether.js"            minified "tether.min.js")   ::
    ("org.webjars" % "bootstrap"   % v.bs    / "bootstrap.js"         minified "bootstrap.min.js" dependsOn ("jquery.js", "tether.js")) ::
    ("org.webjars" % "jquery"      % "2.1.3" / "jquery.js"            minified "jquery.min.js"            commonJSName "JQuery")        ::
    ("org.webjars.bower" % "react" % v.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React")         ::
    ("org.webjars.bower" % "react" % v.react / "react-dom.js"         minified "react-dom.min.js"         commonJSName "ReactDOM"       dependsOn "react-with-addons.js") ::
    ("org.webjars.bower" % "react" % v.react / "react-dom-server.js"  minified "react-dom-server.min.js"  commonJSName "ReactDOMServer" dependsOn "react-dom.js")         ::
    Nil
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := v.scala).
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

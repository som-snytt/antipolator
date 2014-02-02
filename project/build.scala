
import sbt._
import Keys._

trait BuildSettings {
  private val org = "com.github.maqicode"
  private val ver = "0.1"
  private val sca = "2.11.0-M8"
  //private val sca = "2.11.0-SNAPSHOT"
  private val hom = "/home/apm"


  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := org,
    version      := ver,
    scalaVersion := sca,
    //scalaHome    := Some(file("/home/apm/clones/scala/build/pack")),
    //resolvers += "Local Maven Repository" at s"file://$hom/.m2/repository",
    scalacOptions ++= Seq(/*"-Xdev",*/ /*"-Ymacro-debug-verbose",*/ /*"-Xprint:typer,uncurry,lambdalift",*/ "-deprecation", "-feature", "-Xlint", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "com.github.maqicode" %% "test-support" % "0.1" % "test",
      "com.novocode" % "junit-interface" % "0.10" % "test",
      "junit" % "junit" % "4.10" % "test"
    )
  )
}

object AntipolatorBuild extends Build with BuildSettings {

  lazy val rootSettings = buildSettings

  // these fail completely
  //lazy val support = RootProject(uri("git://github.com/som-snytt/test-support.git"))
  //lazy val support = RootProject(uri("https://github.com/som-snytt/test-support"))

  lazy val root = Project("root", file("."), settings = rootSettings) aggregate (core, util)

  lazy val core = Project("core", file("core"), settings = buildSettings) dependsOn(util)

  lazy val util = Project("util", file("util"), settings = buildSettings)
}

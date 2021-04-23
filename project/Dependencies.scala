import sbt._
import Version._

object Dependencies {
  val catsCore   = "org.typelevel" %% "cats-core"   % cats
  val catsEffect = "org.typelevel" %% "cats-effect" % cats

  val meowMtl       = "com.olegpy" %% "meow-mtl-core" % mtl
  val meowMtlEffect = "com.olegpy" %% "meow-mtl-effects" % mtl

  val slf4jSimple  = "org.slf4j"          % "slf4j-simple"  % slf4j
  val log4catsCore = "io.chrisdavenport" %% "log4cats-core" % log4cats
  val log4catsSlf4j = "io.chrisdavenport" %% "log4cats-slf4j"      % log4cats

  val circeCore    = "io.circe" %% "circe-core"    % circe
  val circeGeneric = "io.circe" %% "circe-generic" % circe
  val circeParser  = "io.circe" %% "circe-parser"  % circe

  val http4sDsl         = "org.http4s" %% "http4s-dsl"          % http4s
  val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % http4s
  val http4sCirce       = "org.http4s" %% "http4s-circe"        % http4s

  //val http4sBlazeServer = "org.http4s"        %% "http4s-blaze-server" % http4s

  val fs2core = "co.fs2" %% "fs2-core" % fs2

  val canoeTelegram = "org.augustjune" %% "canoe"       % canoe
}

object Version {
  val mtl           = "0.4.0"
  val cats          = "2.2.0"
  val kindProjector = "0.11.0"
  val slf4j         = "1.7.30"
  val circe         = "0.12.3"
  val fs2           = "2.4.4"
  val log4cats      = "1.1.1"
  val http4s        = "0.21.8"
  val canoe         = "0.5.0"
}
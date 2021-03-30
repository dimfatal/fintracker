import sbt._
import Settings._
import Dependencies._

lazy val telegramBotScenarios = project
  .settings(
    name := "telegram-bot",
    projectSettings,
    compilerOptions,
    baseDependencies)
  .settings(libraryDependencies += canoeTelegram)
  .settings(tests)
  .dependsOn(tcsInvestPrograms)

lazy val tcsInvestClient = project
  .settings(
    name := "broker-client",
    projectSettings,
    compilerOptions,
    baseDependencies
  )
  .settings(circe)
  .settings(http4s)
  .settings(log4cats)
  .settings(tests)

lazy val tcsInvestPrograms = project
  .settings(
    name := "broker-programs",
    projectSettings,
    compilerOptions,
    baseDependencies
  )
  .settings(circe)
  .settings(log4cats)
  .settings(tests)
  .dependsOn(tcsInvestClient)


lazy val `fintracker` = project.in(file("."))
  .settings(
    name := "fintracker",
    compilerOptions,
    projectSettings
  )
  .aggregate(
    tcsInvestClient,
    tcsInvestPrograms,
    telegramBotScenarios
  )

val baseDependencies = libraryDependencies ++= Seq(slf4jSimple, fs2core, catsCore, catsEffect)

val circe = libraryDependencies ++= Seq(circeCore, circeGeneric, circeParser)

val http4s = libraryDependencies ++= Seq(http4sDsl, http4sBlazeClient, http4sCirce)

val log4cats = libraryDependencies ++= Seq(log4catsCore, log4catsSlf4j)

val scalatestVersion = "3.2.2"
lazy val tests = {
  val dependencies =
    libraryDependencies ++= Seq(
      "org.scalatest"              %% "scalatest"                 % scalatestVersion,
//      "org.typelevel"              %% "cats-laws"                 % catsLawsVersion,
//      "org.typelevel"              %% "discipline-scalatest"      % disciplineVersion,
//      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalacheckShapelessVersion
    ).map(_ % Test)

  val frameworks =
    testFrameworks := Seq(TestFrameworks.ScalaTest)

  Seq(dependencies, frameworks)
}
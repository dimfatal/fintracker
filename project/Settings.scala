import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys.{ scalacOptions, _ }
import sbt._
import Version._

object Settings {
  val compilerOptions =
    Seq(
      scalaVersion := "2.13.3",
      scalacOptions := Seq(
        "-Xfatal-warnings",             // Fail the compilation if there are any warnings.
        "-deprecation",                 // Emit warning and location for usages of deprecated APIs.
        "-explaintypes",                // Explain type errors in more detail.
        "-feature",                     // Emit warning and location for usages of features that should be imported explicitly.
        "-language:higherKinds",        // Allow higher-kinded types
        "-language:postfixOps",         // Allow higher-kinded types
        "-language:implicitConversions", // Allow definition of implicit functions called views
         "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
         "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
        "-Ywarn-unused:locals",          // Warn if a local definition is unused.
         "-Ywarn-unused:params", // Warn if a value parameter is unused.
        "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
        "-Ywarn-unused:privates",        // Warn if a private member is unused.
          "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
      ),
      logLevel := Level.Info,
      version := (version in ThisBuild).value,
      scalafmtOnCompile := true,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % kindProjector cross CrossVersion.full)
    )

  //val higherKinds =
  lazy val projectSettings = Seq(
    organization := "dimfatal",
    developers := List(
      Developer("dimfatal", "Dmitry Aleksandrov", "aleksandrov.d89@gmail.com", url("https://github.com/dimfatal"))
    )
  )
}

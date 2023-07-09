import Dependencies._

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.13.6",
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    libraryDependencies ++= Seq(
      L.http4s("ember-server"),
      L.http4s("ember-client"),
      L.http4s("circe"),
      L.http4s("dsl"),
      L.circe,
      L.logback,
      L.pureConfig,
      L.log4cats("core"),
      L.log4cats("slf4j"),
      T.munit,
      C.betterMonadicFor,
      C.kindProjector
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

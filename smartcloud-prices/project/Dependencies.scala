import sbt._

object Dependencies {

  object V { // Versions
    // Scala

    val http4s     = "1.0.0-M29"
    val circe      = "0.15.0-M1"
    val logback    = "1.2.6"
    val pureConfig = "0.16.0"
    val log4cats   = "2.6.0"
    val redis4cats = "1.4.3"

    // Test
    val munit = "0.7.29"
    val munitCatsEffect = "1.0.7"

    // Compiler
    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
  }

  object L { // Libraries
    // Scala
    def http4s(module: String): ModuleID = "org.http4s" %% s"http4s-$module" % V.http4s
    def log4cats(module: String): ModuleID = "org.typelevel" %% s"log4cats-$module" % V.log4cats
    def redis4cats(module: String): ModuleID = "dev.profunktor" %% s"redis4cats-$module" % V.redis4cats
    def circe(module: String): ModuleID = "io.circe" %% s"circe-$module" % V.circe

    val logback    = "ch.qos.logback"         % "logback-classic"     % V.logback
    val pureConfig = "com.github.pureconfig" %% "pureconfig"          % V.pureConfig
  }

  object T { // Test dependencies
    // Scala
    val munit = "org.scalameta"           %% "munit"               % V.munit           % Test
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect % Test
    val log4catsNoOp = "org.typelevel"    %% "log4cats-noop"       % V.log4cats        % Test
  }

  object C { // Compiler plugins
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor)
    val kindProjector    = compilerPlugin("org.typelevel" %% "kind-projector" % V.kindProjector cross CrossVersion.full)
  }

}

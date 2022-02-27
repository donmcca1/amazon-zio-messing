import sbt._

object Dependencies {
  lazy val circeVersion = "0.13.0"
  lazy val circe: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-optics",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-refined"
  ).map(_ % circeVersion)

  lazy val zioVersion = "1.0.12"
  lazy val zio: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio",
    "dev.zio" %% "zio-streams"
  ).map(_ % zioVersion)

  lazy val log4jVersion = "2.17.1"
  lazy val log4j: Seq[ModuleID] = Seq(
    "org.apache.logging.log4j" %  "log4j-api",
    "org.apache.logging.log4j" %  "log4j-core",
    "org.apache.logging.log4j" %  "log4j-slf4j18-impl"
  ).map(_ % log4jVersion) :+ "org.slf4j" %  "slf4j-api"     % "1.8.0-beta4"

  lazy val awsLambda: Seq[ModuleID] = Seq(
    "com.amazonaws" % "aws-lambda-java-core"    % "1.2.1",
    "com.amazonaws" %  "aws-lambda-java-events" % "3.11.0",
    "com.amazonaws" %  "aws-lambda-java-log4j2" % "1.5.0"
  )

  lazy val scanamo: Seq[ModuleID] = Seq(
    "org.scanamo" %% "scanamo"         % "1.0.0-M15",
    "org.scanamo" %% "scanamo-zio"         % "1.0.0-M15",
    "org.scanamo" %% "scanamo-testkit" % "1.0.0-M15"
  )

  lazy val awsDynamo: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" %% "dynamodb" % "2.17.1"
  )
}

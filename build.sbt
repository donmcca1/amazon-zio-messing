
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "amazon-zio-messing",
    idePackagePrefix := Some("org.zio.amazon.messing"),
    libraryDependencies ++= Dependencies.zio,
    libraryDependencies ++= Dependencies.log4j,
    libraryDependencies ++= Dependencies.circe,
    libraryDependencies ++= Dependencies.awsLambda,
    libraryDependencies ++= Dependencies.scanamo,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => xs.map(_.toLowerCase) match {
        case _ :+ "log4j2plugins.dat" => Log4jMergeStrategy.pluginCache
        case List("services", _*) => MergeStrategy.first
        case _ => MergeStrategy.discard
      }
      case _ => MergeStrategy.first
    }
  )

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "br4ve-trave1er.asf",
    idePackagePrefix := Some("org.br4ve.trave1er"),
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.3.2",
      "org.apache.spark" %% "spark-sql" % "3.3.2",
      "org.apache.spark" %% "spark-mllib" % "3.3.2",
      "org.apache.hadoop" % "hadoop-client" % "3.3.2"
    )
  )

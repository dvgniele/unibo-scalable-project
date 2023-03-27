ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val extraFiles = taskKey[Seq[(File, String)]]("Extra files to include in the jar")
extraFiles := Seq((baseDirectory.value / "config", "/"))

lazy val root = (project in file("."))
  .settings(
    name := "br4ve-trave1er.asf",
    idePackagePrefix := Some("org.br4ve.trave1er"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % "2.13.8",
      "org.apache.spark" %% "spark-core" % "3.3.2",
      "org.apache.spark" %% "spark-sql" % "3.3.2",
      "org.apache.spark" %% "spark-mllib" % "3.3.2",
      // already downloaded by gcs-connector but to verify
      "org.apache.hadoop" % "hadoop-common" % "3.3.2",
      "com.google.cloud.bigdataoss" % "gcs-connector" % "1.9.4-hadoop3",
      "com.google.cloud" % "google-cloud-storage" % "2.20.1"
    ),
    assemblyMergeStrategy in Test := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    /*assemblyMergeStrategy := {
      case "plugin.properties" => MergeStrategy.last
      case "log4j.properties" => MergeStrategy.last
      case PathList("META-INF", "log4j-provider.properties") => MergeStrategy.concat
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },*/
    assemblyJarName := "my-application.jar"
  )
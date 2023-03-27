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
    assemblyMergeStrategy in assembly := {
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "log4j.properties" => MergeStrategy.discard
      case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
      case "reference.conf" => MergeStrategy.concat
      case m if m.startsWith("config/") => MergeStrategy.rename
      case _ => MergeStrategy.first
    },
    assemblyJarName := "my-application.jar"
  )
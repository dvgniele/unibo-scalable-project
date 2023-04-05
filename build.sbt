ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.14"

lazy val root = (project in file("."))
  .settings(
    name := "br4ve-trave1er.asf",
    idePackagePrefix := Some("org.br4ve.trave1er"),
    resolvers += "Spark Packages Repo" at "https://repos.spark-packages.org/",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % "2.12.14",
      "org.apache.spark" %% "spark-core" % "3.3.0",
      "org.apache.spark" %% "spark-sql" % "3.3.0",
      "org.apache.spark" %% "spark-mllib" % "3.3.0",
      // already downloaded by gcs-connector but to verify
      "org.apache.hadoop" % "hadoop-common" % "3.3.0",
      "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop3-2.2.10",
      "com.google.cloud.spark" %% "spark-bigquery-with-dependencies" % "0.29.0",
      "Microsoft" % "spark-images" % "0.1"
    ),
    assemblyMergeStrategy in assembly := {
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "log4j.properties" => MergeStrategy.discard
      case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    },
    assemblyJarName := "my-application.jar"
  )
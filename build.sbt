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
      // already downloaded by gcs-connector but to verify
      //"org.apache.hadoop" % "hadoop-client" % "3.3.2",
      "com.google.cloud.bigdataoss" % "gcs-connector" % "1.9.4-hadoop3",
      "com.google.cloud" % "google-cloud-storage" % "2.20.1"
    )
  )

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

/*Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "resources"
Compile / unmanagedResources / includeFilter := "*.json"*/

//Compile / resourceDirectory := baseDirectory.value / "src" / "main" / "resources"
//Compile / resources / includeFilter := "*.json"
//Compile / copyResources / includeFilter := "*.json"

//Compile / packageBin / mappings ++= (Compile / resourceDirectory).value ** "*.json" pair Path.rebase((Compile / resourceDirectory).value, "")
//Compile / packageBin / mappings += file("target/scala-2.13/classes/helloworld-379211-1a6eefa37a81.json") -> ""

Compile / resourceGenerators += Def.task {
      val resourceDir = (Compile / resourceDirectory).value
      val targetDir = (Compile / resourceManaged).value / "main" / "resources"
      val jsonFiles = (resourceDir / "helloworld-379211-1a6eefa37a81.json").get
      println(s"resource directory ${resourceDir}")
      println(s"jsonFiles found: ${jsonFiles.length} + ${jsonFiles}")
      println(s"Target -> $target")
      val mappings = jsonFiles.map {file =>
        val target = targetDir / file.getName
        println(s"Target -> $target")
        println(s"Source file -> $file")
        IO.copyFile (file, target)
        file.getName -> target
        file
      }
      mappings
}.taskValue

Compile / packageBin / mappings ++= ((Compile / resourceManaged).value / "main" / "resources" ** "*").pair(Path.relativeTo(((Compile / resourceManaged).value / "main" / "resources")))

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
      "com.typesafe.play" %% "play-json" % "2.9.4",
      "com.google.auth" % "google-auth-library-oauth2-http" % "1.16.0"
    ),
    assemblyMergeStrategy in assembly := {
      case m if m.endsWith(".json") => println(s"Concat json file $m");MergeStrategy.concat
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "log4j.properties" => MergeStrategy.discard
      case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    },
    assemblyJarName := "my-application.jar"
  )
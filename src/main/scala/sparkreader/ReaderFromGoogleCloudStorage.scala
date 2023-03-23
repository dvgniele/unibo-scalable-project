package org.br4ve.trave1er
package sparkreader
import org.apache.hadoop.fs.{FSDataInputStream, Path}
import org.apache.spark.SparkContext
import org.apache.spark.input.PortableDataStream
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO
import scala.io.Source

class ReaderFromGoogleCloudStorage(path: String) extends ReaderDataset {
  private val contentCred = "./config/helloworld-379211-1a6eefa37a81.json"
  private val jsonBytes = Source.fromFile(contentCred).getLines.mkString
  private val jsonString = java.util.Base64.getEncoder.encode(jsonBytes.getBytes)

  private val spark = SparkSession.builder()
    .appName("SparkGCP")
    .master("local[*]")
    //.config("spark.driver.extraClassPath", "C:\\Users\\notty\\AppData\\Local\\Google\\Cloud SDK\\google-cloud-sdk\\bin")
    //.config("spark.executor.extraClassPath", "C:\\Users\\notty\\AppData\\Local\\Google\\Cloud SDK\\google-cloud-sdk\\bin")
    .config("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    .config("fs.gs.project.id", "helloworld-379211")
    .config("fs.gs.system.bucket", "br4ve-trave1er")
    .config("google.cloud.auth.service.account.enable", "true")
    //.config("spark.hadoop.google.cloud.auth.service.account.email", "br4ve-trave1er-asf@helloworld-379211.iam.gserviceaccount.com")
    .config("google.cloud.auth.service.account.json.keyfile", new String(jsonString))
    .config("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    .config("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    .getOrCreate()

  private val source = s"gs://br4ve-trave1er/dataset";
  private val destination = s"output"
  private val metadataFile = s"dataset/annotations.json"
  private val imagesDirectory = s"images"
  // define the path
  private val imagesDirectoryPath = new Path(source + "/" + path, imagesDirectory)
  private val destinationPath = new Path(source, destination)

  override def readFileMetadata: DataFrame = ???

  override def readFile(filename: String): DataFrame = {
    println("Reading " + filename + " on context " + imagesDirectoryPath.toString)
    val path = new Path(imagesDirectoryPath, filename)
    spark.read.format("image").load(path.toString)
  }

  override def listDirectoryContents(): RDD[(String, PortableDataStream)] = {
    //val test = fs.listFiles(new Path("."), false)
    //val directory = new File(imagesDirectoryPath.toString)
    //directory.listFiles()
    spark.sparkContext.binaryFiles(imagesDirectoryPath.toString)
  }

  override def getImage(inputStream: FSDataInputStream): BufferedImage = {
    val bytes: Array[Byte] = LazyList.continually(inputStream.read()).takeWhile(_ != -1).map(_.toByte).toArray
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def saveImage(filename: String, data: BufferedImage): Unit = {
    val completePath = new Path(destination, filename)
    if (!Files.exists(Paths.get(destinationPath.toString))) {
      println("creating output folder...")
      Files.createDirectory(Paths.get(destinationPath.toString))
    }
    val newFile = new File(completePath.toString)
    ImageIO.write(data, "jpg", newFile)
  }

  override def getSpark(): SparkSession = spark

  override def getSparkContext(): SparkContext = spark.sparkContext
}

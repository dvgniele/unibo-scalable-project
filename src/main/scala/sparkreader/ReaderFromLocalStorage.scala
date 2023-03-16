package org.br4ve.trave1er
package sparkreader
import org.apache.hadoop.conf
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, FileSystem, Path}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO

class ReaderFromLocalStorage(spark: SparkSession, path: String) extends ReaderDataset {
  // Define the string of directories in the dataset
  //private val source = s"./dataset/public_training_set_release_2"
  private val destination = s"./dataset/output"
  private val metadataFile = s"annotations.json"
  private val imagesDirectory = s"images"
  // define the path
  private val imagesDirectoryPath = new Path(path, imagesDirectory)
  private val metadataPath = new Path(path, metadataFile)
  private val destinationPath = new Path(destination)
  private val fs = FileSystem.get(imagesDirectoryPath.toUri, new Configuration())

  //private val fs = FileSystem.get(new Configuration())
  override def readFileMetadata: DataFrame = spark.read
    .format("json")
    .option("multiLine", value = true)
    .option("inferSchema", value = true)
    .load(metadataPath.toString)

  override def readFile(filename: String): DataFrame = {
    //println("Reading " + filename + " on context " + imagesDirectoryPath.toString)
    val path = new Path(imagesDirectoryPath, filename)
    spark.read.format("image").load(path.toString)
  }

  override def listDirectoryContents(): Array[File] = {
    //val test = fs.listFiles(new Path("."), false)
    val directory = new File(imagesDirectoryPath.toString)
    directory.listFiles()
  }

  override def getImage(inputStream: FSDataInputStream): BufferedImage = {
    val bytes: Array[Byte] = LazyList.continually(inputStream.read()).takeWhile(_ != -1).map(_.toByte).toArray
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def saveImage(filename: String, data: BufferedImage): Unit = {
    val completePath = new Path(destination, filename)
    if(!Files.exists(Paths.get(destinationPath.toString))) {
      println("creating output folder...")
      Files.createDirectory(Paths.get(destinationPath.toString))
    }
    val newFile = new File(completePath.toString)
    ImageIO.write(data, "jpg", newFile)
  }
}

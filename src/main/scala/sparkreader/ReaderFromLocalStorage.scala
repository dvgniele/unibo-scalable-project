package org.br4ve.trave1er
package sparkreader
import org.apache.hadoop.conf
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, FileSystem, Path}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.Paths
import javax.imageio.ImageIO

class ReaderFromLocalStorage(spark: SparkSession) extends ReaderDataset {
  // Define the string of directories in the dataset
  private val source = s"./dataset/public_training_set_release_2"
  private val metadataFile = s"annotations.json"
  private val imagesDirectory = s"images"
  // define the path
  private val imagesDirectoryPath = new Path(source, imagesDirectory)
  private val metadataPath = new Path(source, metadataFile)
  private val fs = FileSystem.get(imagesDirectoryPath.toUri, new Configuration())

  //private val fs = FileSystem.get(new Configuration())
  override def readFileMetadata: DataFrame = spark.read
    .format("json")
    .option("multiLine", value = true)
    .option("inferSchema", value = true)
    .load(metadataPath.toString)

  override def readFile(filename: String): FSDataInputStream = {
    println("Reading " + filename + " on context " + imagesDirectoryPath.toString)
    val path = new Path(imagesDirectoryPath, filename)
    fs.open(path)
  }

  override def listDirectoryContents(): Array[FileStatus] = {
    //val test = fs.listFiles(new Path("."), false)
    fs.listStatus(imagesDirectoryPath)
  }

  override def getImage(inputStream: FSDataInputStream): BufferedImage = {
    val bytes: Array[Byte] = LazyList.continually(inputStream.read()).takeWhile(_ != -1).map(_.toByte).toArray
    ImageIO.read(new ByteArrayInputStream(bytes))
  }
}

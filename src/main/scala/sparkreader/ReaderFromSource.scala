package org.br4ve.trave1er
package sparkreader
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ReaderFromSource(rootSource: String, hadoopConfiguration: Configuration, path: String) extends ReaderDataset {

  private val source = rootSource + s"/" + path
  private val destination = s"outputPar"
  private val metadataFile = s"dataset/annotations.json"
  private val imagesDirectory = s"images"
  // define the path
  private val imagesDirectoryPath = new Path(source, imagesDirectory)
  private val destinationPath = new Path(source, destination)

  private val sparkConf = new SparkConf()
    .setAppName("SparkGCP")
    .setMaster("local[*]")

  private val iterator = hadoopConfiguration.iterator()
  while(iterator.hasNext) {
    val entry = iterator.next()
    sparkConf.set(entry.getKey, entry.getValue)
  }

  private val spark = SparkSession.builder()
    .config(sparkConf)
    .getOrCreate()

  checkDestinationFolder

  private def checkDestinationFolder: Unit = {
    val fs = FileSystem.get(destinationPath.toUri, hadoopConfiguration);
    if (fs.exists(destinationPath)) {
      println("creating output folder...")
      fs.mkdirs(destinationPath)
    }
  }


  override def readFileMetadata: DataFrame = ???

  override def readFile(filename: String): DataFrame = {
    println("Reading " + filename + " on context " + imagesDirectoryPath.toString)
    val path = new Path(imagesDirectoryPath, filename)
    spark.read.format("image").load(path.toString)
  }

  override def listDirectoryContents(): Array[FileStatus] = {
    val fs = FileSystem.get(imagesDirectoryPath.toUri, hadoopConfiguration)
    fs.listStatus(imagesDirectoryPath)
  }

  override def saveImage(filename: String, data: BufferedImage): Unit = {
    val completePath = new Path(destinationPath, filename)
    val fs = FileSystem.get(destinationPath.toUri, hadoopConfiguration);
    val newFile = fs.create(completePath)
    try {
      ImageIO.write(data, "jpg", newFile)
    } finally {
      newFile.close()
    }
  }

  override def getSpark(): SparkSession = spark

  override def getSparkContext(): SparkContext = spark.sparkContext
}

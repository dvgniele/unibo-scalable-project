package org.br4ve.trave1er
package sparkreader
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * This class exposes the main operations to perform on storage (cloud or local)
 * @param rootSource path of the source folder
 * @param hadoopConfiguration Configuration for Hadoop
 * @param path name of the current directory (to distiguish between train and test)
 */
class ReaderFromSource(rootSource: String, hadoopConfiguration: Configuration, path: String) extends ReaderDataset {

  // Set the root source and the paths for the metadata file and images directory
  private val source = rootSource + s"/" + path
  private val destination = s"outputPar"
  private val metadataFile = s"dataset/annotations.json"
  private val imagesDirectory = s"images"
  
  // Define paths for images directory and the output directory
  private val imagesDirectoryPath = new Path(source, imagesDirectory)
  private val destinationPath = new Path(source, destination)
  
  // Set up the Spark configuration
  private val sparkConf = new SparkConf()
    .setAppName("SparkGCP")
    .setMaster("local[*]")
  
  // Set the Hadoop configuration properties in the Spark configuration
  private val iterator = hadoopConfiguration.iterator()
  while(iterator.hasNext) {
    val entry = iterator.next()
    sparkConf.set(entry.getKey, entry.getValue)
  }
  
  // Set up the Spark session
  private val spark = SparkSession.builder()
    .config(sparkConf)
    .getOrCreate()

  // Check if the output directory exists; if it does not exist, create it
  checkDestinationFolder()

  private def checkDestinationFolder(): Unit = {
    val fs = FileSystem.get(destinationPath.toUri, hadoopConfiguration);
    if (fs.exists(destinationPath)) {
      println("creating output folder...")
      fs.mkdirs(destinationPath)
    }
  }

  // Return an empty DataFrame for reading metadata (not implemented in this class)
  override def readFileMetadata: DataFrame = ???

  // Read a file and return it as a DataFrame
  override def readFile(filename: String): DataFrame = {
    println("Reading " + filename + " on context " + imagesDirectoryPath.toString)
    val path = new Path(imagesDirectoryPath, filename)
    spark.read.format("image").load(path.toString)
  }

  // List the contents of the images directory
  override def listDirectoryContents(): Array[FileStatus] = {
    val fs = FileSystem.get(imagesDirectoryPath.toUri, hadoopConfiguration)
    fs.listStatus(imagesDirectoryPath)
  }

  // Save an image to the output directory
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

  // Return the Spark session
  override def getSpark(): SparkSession = spark

  // Return the Spark context
  override def getSparkContext(): SparkContext = spark.sparkContext
}

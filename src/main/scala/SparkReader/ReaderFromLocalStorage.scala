package org.br4ve.trave1er
package SparkReader
import org.apache.hadoop.conf
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}

class ReaderFromLocalStorage(spark: SparkSession) extends ReaderDataset {
  private val source = s"./dataset/public_training_set_release_2"
  private val metadataFile = s"annotations.json"
  private val imagesDirectory = new Path(source + s"/images")
  private val metadataPath = source + s"/" + metadataFile

  //private val fs = FileSystem.get(new Configuration())
  override def readFileMetadata: DataFrame = spark.read
    .format("json")
    .option("multiLine", value = true)
    .option("inferSchema", value = true)
    .load(metadataPath)

  override def readFile(filename: String): DataFrame = ???

  override def listDirectoryContents(): Array[FileStatus] = {
    val fs = FileSystem.get(imagesDirectory.toUri, new Configuration())
    val test = fs.listFiles(new Path("."), false)
    fs.listStatus(imagesDirectory)
  }
}

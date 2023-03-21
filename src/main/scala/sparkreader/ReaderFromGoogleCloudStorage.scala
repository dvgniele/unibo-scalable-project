package org.br4ve.trave1er
package sparkreader
import org.apache.hadoop.fs.FSDataInputStream
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.awt.image.BufferedImage
import java.io.File

/*
*
* hadoopConf.set("google.cloud.auth.service.account.enable", "true")
hadoopConf.set("google.cloud.auth.service.account.json.keyfile", "/path/to/keyfile.json")
hadoopConf.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
hadoopConf.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
* */

class ReaderFromGoogleCloudStorage extends ReaderDataset {
  private val spark = SparkSession.builder()
    .appName("SparkGCP")
    .master("local[*]")
    .config("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    .config("spark.hadoop.fs.gs.project.id", "<project-id>")
    .config("google.cloud.auth.service.account.enable", "true")
    .config("google.cloud.auth.service.account.json.keyfile", "/path/to/keyfile.json")
    .config("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    .config("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    .getOrCreate()
  override def readFileMetadata: DataFrame = ???

  override def readFile(filename: String): DataFrame = ???

  override def listDirectoryContents(): Array[File] = ???

  override def getImage(inputStream: FSDataInputStream): BufferedImage = ???

  override def saveImage(filename: String, data: BufferedImage): Unit = ???

  override def getSpark(): SparkSession = spark

  override def getSparkContext(): SparkContext = spark.sparkContext
}

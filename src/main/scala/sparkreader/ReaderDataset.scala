package org.br4ve.trave1er
package sparkreader

import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, FileSystem}
import org.apache.spark.SparkContext
import org.apache.spark.input.PortableDataStream
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

trait ReaderDataset {
  def readFileMetadata: DataFrame
  def readFile(filename: String): DataFrame
  def listDirectoryContents(): Array[FileStatus];
  def getImage(inputStream: FSDataInputStream): BufferedImage
  def saveImage(filename: String, data: BufferedImage): Unit
  def getSpark(): SparkSession
  def getSparkContext(): SparkContext

}

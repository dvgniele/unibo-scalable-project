package org.br4ve.trave1er
package sparkreader

import org.apache.hadoop.fs.FileStatus
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.awt.image.BufferedImage

trait ReaderDataset {
  def readFileMetadata: DataFrame
  def readFile(filename: String): DataFrame
  def listDirectoryContents(): Array[FileStatus];
  def saveImage(filename: String, data: BufferedImage): Unit
  def getSpark(): SparkSession
  def getSparkContext(): SparkContext

}

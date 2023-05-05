package org.br4ve.trave1er
package sparkreader

import org.apache.hadoop.fs.FileStatus
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.awt.image.BufferedImage

trait ReaderDataset {
  /**
   * Not implemented
   * @return
   */
  def readFileMetadata: DataFrame

  /**
   * Read a file as a dataframe inside the selected directory
   * @param filename the name of the file to read
   * @return the dataframe of the file read
   */
  def readFile(filename: String): DataFrame

  /**
   * List all object inside the current directory
   * @return an array of each element inside the directory (as a FileStatus object)
   */
  def listDirectoryContents(): Array[FileStatus];

  /**
   * Store the buffer data to the storage (local or remote)
   * @param filename name of the new generated file
   * @param data the buffer of data to write inside of filename
   */
  def saveImage(filename: String, data: BufferedImage): Unit

  /**
   * 
   * @return current spark session object
   */
  def getSpark(): SparkSession

  /**
   * 
   * @return current spark context object
   */
  def getSparkContext(): SparkContext

}

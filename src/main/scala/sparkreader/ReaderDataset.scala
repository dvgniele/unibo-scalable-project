package org.br4ve.trave1er
package sparkreader

import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, FileSystem}
import org.apache.spark.sql.DataFrame

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

trait ReaderDataset {

  def readFileMetadata: DataFrame
  def readFile(filename: String): DataFrame
  def listDirectoryContents(): Array[File]
  def getImage(inputStream: FSDataInputStream): BufferedImage
  def saveImage(filename: String, data: Array[Byte]): Unit

}

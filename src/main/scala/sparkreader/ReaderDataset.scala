package org.br4ve.trave1er
package sparkreader

import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, FileSystem}
import org.apache.spark.sql.DataFrame

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

trait ReaderDataset {

  def readFileMetadata: DataFrame
  def readFile(filename: String): FSDataInputStream
  def listDirectoryContents(): Array[FileStatus];
  def getImage(inputStream: FSDataInputStream): BufferedImage;

}

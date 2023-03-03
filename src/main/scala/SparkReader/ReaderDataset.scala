package org.br4ve.trave1er
package SparkReader

import org.apache.hadoop.fs.{FileStatus, FileSystem}
import org.apache.spark.sql.DataFrame

trait ReaderDataset {

  def readFileMetadata: DataFrame
  def readFile(filename: String): DataFrame
  def listDirectoryContents(): Array[FileStatus];

}

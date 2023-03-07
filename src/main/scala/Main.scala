package org.br4ve.trave1er

import org.apache.spark.sql.SparkSession
import org.br4ve.trave1er.SparkReader.ReaderFromLocalStorage

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

object Main {
  def main(args: Array[String]): Unit = {
    // configure spark
    val spark = SparkSession.builder()
      .appName("ReadJsonFile")
      .master("local[*]")
      .getOrCreate()
    // create the reader of FileSystem (hadoop)
    val reader = new ReaderFromLocalStorage(spark)
    //list the file inside the images folder
    val results = reader.listDirectoryContents()
    results.foreach(x => {
      // read the input stream and convert it to Array of bytes
      val inputStream = reader.readFile(x.getPath.getName)
      //get the image object
      val image = reader.getImage(inputStream)
      println("width: " + image.getWidth + " height: " + image.getHeight)
      // todo continue with kmenas
    })
  }
}
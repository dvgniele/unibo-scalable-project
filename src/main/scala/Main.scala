package org.br4ve.trave1er

import org.apache.spark.sql.SparkSession
import org.br4ve.trave1er.SparkReader.ReaderFromLocalStorage

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ReadJsonFile")
      .master("local[*]")
      .getOrCreate()
    val reader = new ReaderFromLocalStorage(spark)
    val results = reader.listDirectoryContents()
    println("Hello world!")
    //println(results)
    results.foreach(x=> println(x.getPath))
    //println(results)
  }
}
package org.br4ve.trave1er

import org.apache.spark.sql.SparkSession

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ReadJsonFile")
      .master("local[*]")
      .getOrCreate()
    println("Hello world!")
  }
}
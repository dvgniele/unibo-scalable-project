package org.br4ve.trave1er

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, collect_list}
import org.br4ve.trave1er.Preprocessing.{ImagePreprocessingUtils, PreprocessedImage}
import org.br4ve.trave1er.sparkreader.ReaderFromLocalStorage
import org.br4ve.trave1er.segmentation.ImageSegmentation

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util
import javax.imageio.ImageIO
import scala.collection.mutable.WrappedArray
import scala.collection.parallel.CollectionConverters.ArrayIsParallelizable

object Main {
  def main(args: Array[String]): Unit = {
    // configure spark
    val spark = SparkSession.builder()
      .appName("ReadJsonFile")
      .master("local[*]")
      .getOrCreate()
    // create the reader of FileSystem (hadoop)

    val reader = new ReaderFromLocalStorage(spark)

    //  reading all files in dataset directory
    val files_list = reader.listDirectoryContents()
    //  print of filenames (in parallel exec)
    //files_list.par.foreach(file => println(file.getName))

    val k = 3
    val model = new ImageSegmentation(k)

    var train_set = List.empty[DataFrame]

    files_list.par.foreach(file => {
      val image_df = reader.readFile(file.getName)
      println("Starting segmentation on file: " + file.getName)

      //val pp_image = new ImagePreprocessingUtils(image_df, spark)
      val pp_tuple = PreprocessedImage.decodeImageDataFrame(spark, image_df)
      val pp_image = pp_tuple._1
      val pp_width = pp_tuple._2
      val pp_height = pp_tuple._3
      //val pp_df = pp_image.getTransformedDataFrame

      val fitted = model.modelFit(pp_image)
      val prediction = model.transformData(pp_image, fitted)
      val image = model.getSegmentedImage(prediction, pp_width, pp_height)

      ImageIO.write(image, "jpg", new File("dataset/output/" + file.getName))

      //train_set = train_set :+ pp_df
    })


    //val testa = train_set.head
    //val fitted = model.modelFit(testa)

    //val prediction = model.transformData(testa, fitted)

    //val image = model.getSegmentedImage(prediction, prediction.select("w")., prediction.select("h"))



    return

  }
}
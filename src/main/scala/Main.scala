package org.br4ve.trave1er

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, collect_list}
import org.br4ve.trave1er.sparkreader.ReaderFromLocalStorage

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.collection.mutable.WrappedArray

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
    val image_df = reader.readFile("006316.jpg")
    println(image_df)
    val image_array: RDD[Byte] = image_df.select(col("image.data"))
      .rdd.flatMap(f => f.getAs[Array[Byte]]("data"))

    val image_data = image_df.select(col("image.*"))
      .rdd.map(row => (
      row.getAs[Int]("height"),
      row.getAs[Int]("width"),
      row.getAs[Int]("nChannels"),
      row.getAs[Byte]("data")
    ))
      .collect()(0)

    val height = image_data._1
    val width = image_data._2
    val nChannels = image_data._3

    var offSet = spark.sparkContext.longAccumulator("offSetAcc")
    var x = spark.sparkContext.longAccumulator("xAcc")
    var y = spark.sparkContext.longAccumulator("yAcc")
    x.add(1)
    y.add(1)

    import spark.implicits._
    var final_image_df = image_array.zipWithIndex().map { f =>

      if (offSet.value == 0) {
        offSet.add(1)
        if (f._2 != 0)
          x.add(1)
      } else if (offSet.value == 1) {
        //g
        offSet.add(1)
      } else if (offSet.value == 2) {
        //r
        offSet.reset()
      }
      if (x.value == (width)) {
        x.reset()
        y.add(1)
      }

      (f._1 & 0xFF, x.value, y.value)
    }.toDF
      .withColumnRenamed("_1", "color")
      .withColumnRenamed("_2", "w")
      .withColumnRenamed("_3", "h")

    final_image_df = final_image_df.groupBy(col("w"), col("h"))
      .agg(collect_list(col("color")).as("color"))
      .orderBy(col("w"), col("h"))
      .rdd
      .map { f =>
        val a = f(2).asInstanceOf[WrappedArray[Int]]
        (f(0).toString.toInt, f(1).toString.toInt, a(0), a(1), a(2))
      }
      .toDF
      .withColumnRenamed("_1", "w")
      .withColumnRenamed("_2", "h")
      .withColumnRenamed("_3", "b")
      .withColumnRenamed("_4", "g")
      .withColumnRenamed("_5", "r")

    val features_col = Array(
      //            "w","h",
      "b", "g", "r")
    val vector_assembler = new VectorAssembler()
      .setInputCols(features_col)
      .setOutputCol("features")

    val va_transformed_df = vector_assembler.transform(final_image_df)

    val k = 3
    val kmeans = new KMeans().setK(k).setSeed(1).setFeaturesCol("features")
    val kmeans_model = kmeans.fit(va_transformed_df)

    val va_cluster_df = kmeans_model.transform(va_transformed_df)
      .select("w", "h", "b", "g", "r", "prediction")

    var colors: Map[Int, (Int, Int, Int)] = Map[Int, (Int, Int, Int)]()

    for (i <- 0 to k - 1) {
      val s = va_cluster_df.filter(col("prediction") === i).rdd.first
      colors += (i -> (s(2).asInstanceOf[Int], s(3).asInstanceOf[Int], s(4).asInstanceOf[Int]))
    }
    val image_array_final = va_cluster_df.select("w", "h", "prediction")
      .rdd.map(f => (f.getAs[Int](0), f.getAs[Int](1), f.getAs[Int](2))).collect()

    // create new image of the same size
    val out = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)

    for (j <- 0 until image_array_final.size - 1) {
      val color_tuple = colors(image_array_final(j)._3)
      out.setRGB(image_array_final(j)._1, image_array_final(j)._2,
        new Color(color_tuple._3, color_tuple._2, color_tuple._1).getRGB)
    }
    ImageIO.write(out, "jpg", new File("dataset/output/test.jpg"))
  }
}
package org.br4ve.trave1er

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.mllib.linalg.VectorUDT
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{col, collect_list}
import org.apache.spark.sql.types.{ArrayType, DoubleType, FloatType, IntegerType, StringType, StructField, StructType}
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

		val reader = new ReaderFromLocalStorage(spark, "./dataset/testino")

		//  reading all files in dataset directory
		val files_list = reader.listDirectoryContents()
		//  print of filenames (in parallel exec)
		//files_list.par.foreach(file => println(file.getName))

		val k = 3
		val model = new ImageSegmentation(k)

		/*
		val features_array = spark.sparkContext.parallelize(files_list)
			.map(
				file => {
					val image_df = reader.readFile(file.getName)
					println("Starting segmentation on file: " + file.getName)

					//val pp_image = new ImagePreprocessingUtils(image_df, spark)
					PreprocessedImage.decodeImageDataFrame(spark, image_df)
				})
		 */

		var train_set = List.empty[(DataFrame, Int, Int)]

		files_list.par.foreach(file => {
			val image_df = reader.readFile(file.getName)
			println("Starting segmentation on file: " + file.getName)

			val pp_tuple = PreprocessedImage.decodeImageDataFrame(spark, image_df)

			train_set = train_set :+ pp_tuple
		})


		// Define the schema for the DataFrame
		/*
		val schema = StructType(Seq(
			StructField("features", ArrayType(StringType))
		))
		 */

		var schema: StructType = null

		/*
		val rdd = train_set.map(item => {
			print("bl")
			if (schema == null) {
				schema = item._1.schema
				println("DEFINITOOOOOOOOOO")
				println("DEFINITOOOOOOOOOO")
				println("DEFINITOOOOOOOOOO")
			}
			Row(item._1)
		})

		 */

		/*
		val rdd = spark.sparkContext.parallelize(train_set).map(item => {
			if (schema == null) {
				schema = item._1.schema
			}
			Row(item._1)
		}
		).collect()

		 */

		val rows = train_set.map(item => {
			if (schema == null) {
				schema = item._1.schema
			}
			Row(item._1)
		})

		val rdd = spark.sparkContext.parallelize(rows)

		val df = spark.createDataFrame(rdd, schema)

		val properties = spark.sparkContext.parallelize(train_set).map { item =>
			Row.fromTuple((item._2, item._3))
		}

		val fitted = model.modelFit(df)

		val reader_test = new ReaderFromLocalStorage(spark, "./dataset/testino/test")

		val test_files_list = reader_test.listDirectoryContents()
		test_files_list.par.foreach(file => {
			val image_df = reader_test.readFile(file.getName)
			println("Starting segmentation on file: " + file.getName)

			val pp_tuple = PreprocessedImage.decodeImageDataFrame(spark, image_df)

			val pp_image = pp_tuple._1
			val pp_width = pp_tuple._2
			val pp_height = pp_tuple._3

			println("Predicting image: " + file.getName)
			val prediction = model.transformData(pp_image, fitted)
			val image = model.getSegmentedImage(prediction, pp_width, pp_height)

			reader_test.saveImage(file.getName, image)
		})



		//val testa = train_set.head
		//val fitted = model.modelFit(testa)

		//val prediction = model.transformData(testa, fitted)

		//val image = model.getSegmentedImage(prediction, prediction.select("w")., prediction.select("h"))



		return

	}
}
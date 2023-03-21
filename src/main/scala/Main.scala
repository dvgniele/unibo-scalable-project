package org.br4ve.trave1er

import Preprocessing.PreprocessedImage
import segmentation.ImageSegmentation
import sparkreader.ReaderFromLocalStorage

import org.apache.spark.sql.{Row, SparkSession}

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

		val k = 3
		val model = new ImageSegmentation(k)

		val train_set = files_list.par.map(file => {
			val image_df = reader.readFile(file.getName)
			println("Starting segmentation on file: " + file.getName)

			val pp_tuple = PreprocessedImage.decodeImageDataFrame(spark, image_df)
			pp_tuple
		})

		val rows = train_set.map(item => {
			item._1
		})

		val df = rows.reduce(_ unionAll _)

		val properties = train_set.map { item =>
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

	}
}
package org.br4ve.trave1er

import Preprocessing.PreprocessedImage
import hadoopConfigurationBuilder.HadoopConfigurationBuilder
import segmentation.ImageSegmentation
import sparkreader.ReaderFromSource

import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.{avg, col}

import java.awt.image.BufferedImage
import java.io.File

object Main {
	def main(args: Array[String]): Unit = {
		// start the timer
		val startTime = System.currentTimeMillis()

		val reader = new ReaderFromSource(HadoopConfigurationBuilder.getRemoteSource, HadoopConfigurationBuilder.getHadoopConfigurationForGoogleCloudPlatform, "train")
		
		//  reading all files in dataset directory
		val files_list_df = reader.listDirectoryContents()

		val k = 2
		val model = new ImageSegmentation(k)
		
		val train_set = files_list_df.par.map(file => {
			val completePath = file.getPath
			val fileHandler = new File(completePath.toString)
			println("Starting segmentation on file: " + fileHandler.getName)
			val image_df = reader.readFile(fileHandler.getName)
			val pp_tuple = PreprocessedImage.decodeImageDataFrame(reader.getSpark(), image_df)
			pp_tuple
		})

		val rows = train_set.map(item => {
			item._1
		})
		
		val df = rows.reduce(_ unionAll _).repartition(24)
		
		val properties = train_set.map { item =>
			Row.fromTuple((item._2, item._3))
		}
		
		val fitted = model.modelFit(df)
		
		//val reader_test = new ReaderFromGoogleCloudStorage("test")
		val reader_test = new ReaderFromSource(HadoopConfigurationBuilder.getRemoteSource, HadoopConfigurationBuilder.getHadoopConfigurationForGoogleCloudPlatform, "test")
		
		val test_files_list = reader_test.listDirectoryContents()
		val silhouette = Array.ofDim[Double](test_files_list.length)
		test_files_list.par.zipWithIndex.map{
			case (file, index) => {
				val completePath = file.getPath
				val fileHandler = new File(completePath.toString)
				val image_df = reader_test.readFile(fileHandler.getName)
				println("Starting segmentation on file: " + fileHandler.getName)

				val pp_tuple = PreprocessedImage.decodeImageDataFrame(reader_test.getSpark(), image_df)

				val pp_image = pp_tuple._1
				val pp_width = pp_tuple._2
				val pp_height = pp_tuple._3

				println("Predicting image: " + fileHandler.getName)
				val prediction = model.transformData(pp_image, fitted)
				silhouette(index) = model.evaluate(prediction)
				val image: BufferedImage = model.getSegmentedImage(prediction, pp_width, pp_height)

				val neutral = Vectors.dense(0.0, 0.0, 0.0)
				val filledCentroids = Array.ofDim[Vector](k)
				val centroids = prediction.groupBy("prediction").agg(
					avg(col("r")).as("r"),
					avg(col("g")).as("g"),
					avg(col("b")).as("b")).collect().map(row => Vectors.dense(math.round(row.getDouble(1)), math.round(row.getDouble(2)), math.round(row.getDouble(3))))

				for (i <- centroids.indices) {
					filledCentroids(i) = centroids(i)
				}
				for (i <- centroids.length until k) {
					filledCentroids(i) = neutral
				}

				reader_test.saveImage(fileHandler.getName, image)
				(fileHandler.getName, image: BufferedImage)
			}
		}

		val endTime = System.currentTimeMillis()
		val executionTime = endTime - startTime // convert to milliseconds

		val hours = executionTime / (1000 * 60 * 60)
		val minutes = (executionTime / (1000 * 60)) % 60
		val seconds = (executionTime / 1000) % 60

		val meanSilhouette = silhouette.sum / silhouette.length
		println(s"Silhouette: $meanSilhouette")

		println(s"Execution time: $executionTime ms")
		println(s"Execution time: $hours hours $minutes minutes and $seconds s")
	}
}
package org.br4ve.trave1er

import Preprocessing.PreprocessedImage
import hadoopConfigurationBuilder.HadoopConfigurationBuilder
import segmentation.ImageSegmentation
import sparkreader.ReaderFromSource

import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.{avg, col}

import java.awt.image.BufferedImage
import java.io.File

object Main {
	def main(args: Array[String]): Unit = {
		
		//val reader = new ReaderFromGoogleCloudStorage("train")
		val reader = new ReaderFromSource(HadoopConfigurationBuilder.getRemoteSource, HadoopConfigurationBuilder.getHadoopConfigurationForGoogleCloudPlatform, "train")
		
		//  reading all files in dataset directory
		val files_list_df = reader.listDirectoryContents()
		//val files_list_rdd = files_list_df.rdd
		val k = 3
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
		
		val df = rows.reduce(_ unionAll _)
		
		val properties = train_set.map { item =>
			Row.fromTuple((item._2, item._3))
		}
		
		val fitted = model.modelFit(df)
		
		//val reader_test = new ReaderFromGoogleCloudStorage("test")
		val reader_test = new ReaderFromSource(HadoopConfigurationBuilder.getRemoteSource, HadoopConfigurationBuilder.getHadoopConfigurationForGoogleCloudPlatform, "test")
		
		val test_files_list = reader_test.listDirectoryContents()
		test_files_list.par.map(file => {
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
			val image: BufferedImage = model.getSegmentedImage(prediction, pp_width, pp_height)
			
			val centroids = prediction.groupBy("prediction").agg(
				avg(col("r")).as("r"),
				avg(col("g")).as("g"),
				avg(col("b")).as("b")).collect().map(row => Vectors.dense(math.round(row.getDouble(1)), math.round(row.getDouble(2)), math.round(row.getDouble(3))))
			val data =  prediction.select("r", "g", "b").rdd.map(row => Vectors.dense(row.getInt(0), row.getInt(1), row.getInt(2))).collect()
			val clusterAssignments = prediction.select("prediction").rdd.map(_.getInt(0)).collect()
			var sse = 0.0
			for (i <- data.indices) {
				val point = data(i)
				val centroid = centroids(clusterAssignments(i))
				val distance = computeDistance(point, centroid)
				sse += distance * distance
			}
			println(s"SSE value: $sse")
			
			def computeDistance(v1: Vector, v2: Vector): Double = {
				math.sqrt(Vectors.sqdist(v1, v2))
			}
			reader_test.saveImage(fileHandler.getName, image)
			(fileHandler.getName, image: BufferedImage
			)
		})
	}
}
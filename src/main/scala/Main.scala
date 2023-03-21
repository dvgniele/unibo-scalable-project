package org.br4ve.trave1er

import Preprocessing.PreprocessedImage
import segmentation.ImageSegmentation
import sparkreader.ReaderFromLocalStorage

import breeze.linalg.*
import org.apache.spark.sql.functions.{avg, col}
import org.apache.spark.sql.Row
import org.apache.spark.ml.linalg.{Vector, Vectors}

import scala.collection.parallel.CollectionConverters.ArrayIsParallelizable

object Main {
	def main(args: Array[String]): Unit = {
		
		val reader = new ReaderFromLocalStorage("./dataset/testino/")
		
		//  reading all files in dataset directory
		val files_list = reader.listDirectoryContents()
		
		val k = 3
		val model = new ImageSegmentation(k)
		
		val train_set = files_list.par.map(file => {
			val image_df = reader.readFile(file.getName)
			println("Starting segmentation on file: " + file.getName)
			
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
		
		val reader_test = new ReaderFromLocalStorage("./dataset/testino/test")
		
		val test_files_list = reader_test.listDirectoryContents()
		test_files_list.par.foreach(file => {
			val image_df = reader_test.readFile(file.getName)
			println("Starting segmentation on file: " + file.getName)
			
			val pp_tuple = PreprocessedImage.decodeImageDataFrame(reader_test.getSpark(), image_df)
			
			val pp_image = pp_tuple._1
			val pp_width = pp_tuple._2
			val pp_height = pp_tuple._3
			
			println("Predicting image: " + file.getName)
			val prediction = model.transformData(pp_image, fitted)
			val image = model.getSegmentedImage(prediction, pp_width, pp_height)
			
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
			
			reader_test.saveImage(file.getName, image)
		})
		
	}
}
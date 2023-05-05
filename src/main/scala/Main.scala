package org.br4ve.trave1er

import Preprocessing.PreprocessedImage
import hadoopConfigurationBuilder.HadoopConfigurationBuilder
import segmentation.ImageSegmentation
import sparkreader.ReaderFromSource

import org.apache.spark.sql.Row

import java.awt.image.BufferedImage
import java.io.File

object Main {
	def main(args: Array[String]): Unit = {
		// start the timer
		val startTime = System.currentTimeMillis()

		// Initialize reader object with remote source and hadoop configuration for Google Cloud Platform 
		val reader = new ReaderFromSource(HadoopConfigurationBuilder.getRemoteSource, HadoopConfigurationBuilder.getHadoopConfigurationForGoogleCloudPlatform, "train")
		
		// List all files in the dataset directory
		val files_list_df = reader.listDirectoryContents()

		// create the kmeans model to fit
		val k = 2
		val model = new ImageSegmentation(k)

		// Dataframe of images that each row contains the rgb and width/height information
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


		val reader_test = new ReaderFromSource(HadoopConfigurationBuilder.getRemoteSource, HadoopConfigurationBuilder.getHadoopConfigurationForGoogleCloudPlatform, "test")

		// Create the dataframe of images for the evaluation test (test)
		val test_files_list = reader_test.listDirectoryContents()
		// Declare the array of silhouette for each images in test dataset (necessary to calculate the mean)
		val silhouette = Array.ofDim[Double](test_files_list.length)

		// run the evaluation for each element
		test_files_list.par.zipWithIndex.map{
			case (file, index) => {
				val completePath = file.getPath
				val fileHandler = new File(completePath.toString)
				val image_df = reader_test.readFile(fileHandler.getName)
				println("Starting segmentation on file: " + fileHandler.getName)

				val pp_tuple = PreprocessedImage.decodeImageDataFrame(reader_test.getSpark(), image_df)

				// Read in the image data and preprocess it
				val pp_image = pp_tuple._1
				val pp_width = pp_tuple._2
				val pp_height = pp_tuple._3

				// Predict the segmentation of the image and evaluate the silhouette score
				println("Predicting image: " + fileHandler.getName)
				val prediction = model.transformData(pp_image, fitted)
				silhouette(index) = model.evaluate(prediction)
				// Get the segmented image and save it to file
				val image: BufferedImage = model.getSegmentedImage(prediction, pp_width, pp_height)

				reader_test.saveImage(fileHandler.getName, image)
				// Return the filename and image object for debugging purposes
				(fileHandler.getName, image: BufferedImage)
			}
		}
		
		// Get the end time of the execution
		val endTime = System.currentTimeMillis()

		// Calculate the execution time by subtracting the start time from the end time
		val executionTime = endTime - startTime // convert to milliseconds

		// Calculate the hours, minutes, and seconds from the execution time
		val hours = executionTime / (1000 * 60 * 60)
		val minutes = (executionTime / (1000 * 60)) % 60
		val seconds = (executionTime / 1000) % 60

		// Calculate the mean silhouette score across all test images
		val meanSilhouette = silhouette.sum / silhouette.length
		println(s"Silhouette: $meanSilhouette")
		
		// Print the execution time in milliseconds, hours, minutes, and seconds
		println(s"Execution time: $executionTime ms")
		println(s"Execution time: $hours hours $minutes minutes and $seconds s")
	}
}
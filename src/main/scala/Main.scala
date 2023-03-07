package org.br4ve.trave1er

import org.apache.spark.sql.SparkSession
import org.br4ve.trave1er.segmentation.ImageSegmentation
import org.br4ve.trave1er.sparkreader.ReaderFromLocalStorage
import org.br4ve.trave1er.utils.Utils.images2PixelMatrix

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

object Main {
	def main(args: Array[String]): Unit = {
		// configure spark
		val spark = SparkSession.builder()
			.appName("ReadJsonFile")
			.master("local[*]")
			.getOrCreate()

		val segmentator = new ImageSegmentation(spark = spark)

		// create the reader of FileSystem (hadoop)
		val reader = new ReaderFromLocalStorage(spark)
		//list the file inside the images folder
		val results = reader.listDirectoryContents()

		var images = new Array[BufferedImage](0)

		results.foreach(x => {
			// read the input stream and convert it to Array of bytes
			val inputStream = reader.readFile(x.getPath.getName)
			//get the image object
			val image = reader.getImage(inputStream)
			println("width: " + image.getWidth + " height: " + image.getHeight)

			images :+= image
		})

		// todo continue with kmeans
		val matrix = images2PixelMatrix(images)

		val model = segmentator.run(matrix)
		val predictions = segmentator.predict(matrix, model)

		//segmentator.saveSegmentedImages(predictions, "./dataset/output")




	}
}
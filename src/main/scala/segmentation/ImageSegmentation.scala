package org.br4ve.trave1er
package segmentation

import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import javax.imageio.ImageIO


class ImageSegmentation(k: Int = 10, maxIterations: Int = 100, seed: Long = 1L, spark: SparkSession) {
	private val KMeans = new KMeans()
		.setK(k)
		.setMaxIterations(maxIterations)
		.setSeed(seed)

	private val sc = spark.sparkContext
	def run(imgs: Array[Array[Double]]) = {
		val data: RDD[Vector] = sc.parallelize(imgs).map(x => Vectors.dense(x))

		KMeans.run(data)
	}

	def predict(imgs: Array[Array[Double]], model: KMeansModel): Array[Int] = {
		val data: RDD[Vector] = sc.parallelize(imgs).map(x => Vectors.dense(x))
		model.predict(data).collect()
	}

	// todo: test and correct
	def saveSegmentedImages(segmentedData: RDD[(String, Array[Double])], outputPath: String): Unit = {
		segmentedData.foreach { case (fileName, pixels) =>
			// Create BufferedImage
			val width = ??? // specify the width of the image
			val height = ??? // specify the height of the image
			val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

			// Set pixels
			val rgbPixels = pixels.map(_.toInt).toArray
			bufferedImage.setRGB(0, 0, width, height, rgbPixels, 0, width)

			// Save image to file
			val outputFilePath = s"$outputPath/${new File(fileName).getName}"
			val outputStream = new ByteArrayOutputStream()
			ImageIO.write(bufferedImage, "jpg", outputStream)
			FileUtils.writeByteArrayToFile(new File(outputFilePath), outputStream.toByteArray)
		}
	}

}

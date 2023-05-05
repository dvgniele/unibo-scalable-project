package org.br4ve.trave1er
package segmentation

import org.apache.spark.ml.clustering.{BisectingKMeans, BisectingKMeansModel, KMeans, KMeansModel}
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

import java.awt.Color
import java.awt.image.BufferedImage

/**
 * This class are implemented some useful method to interact with the kmenas algorithms (implemented by MLlib)
 * @param k number of cluster in Bisecting Kmeans
 */
class ImageSegmentation(k: Int = 17) extends IImageSegmentation{
	// initialize the ClusteringEvaluator instance for evaluating the model	
	private val evaluator = new ClusteringEvaluator()

	// initialize the BisectingKMeans instance with the specified k value and features column
	private val kmeans = new BisectingKMeans()
		.setK(k)
		.setSeed(1L)
		.setFeaturesCol("features")

	// fit the data to the model and return the BisectingKMeansModel instance
	def modelFit(data: DataFrame): BisectingKMeansModel = {
		kmeans.fit(data)
	}

	// transform the data using the model and select the specified columns
	def transformData(data: DataFrame, model: BisectingKMeansModel): DataFrame = {
		model.transform(data)
			.select("w", "h", "b", "g", "r", "features", "prediction")
	}

	// evaluate the model using the specified data and return the evaluation score
	def evaluate(data: DataFrame): Double = {
		evaluator.evaluate(data)
	}

	// get the segmented image using the specified data and dimensions
	def getSegmentedImage(data: DataFrame, width: Int, height: Int): BufferedImage = {

		// initialize an empty map to store the colors of each cluster
		var colors: Map[Int, (Int, Int, Int)] = Map[Int, (Int, Int, Int)]()
		
		// iterate over each cluster
		for (i <- 0 until k) {
			// filter the data by the cluster's prediction and convert it to an RDD
			val cluster = data.filter(col("prediction") === i).rdd
			// if the cluster is not empty, get the first record and add its color to the colors map
			if (!cluster.isEmpty) {
				val s = cluster.first
				colors += (i -> (s(2).asInstanceOf[Int], s(3).asInstanceOf[Int], s(4).asInstanceOf[Int]))
			}
		}

		// create an array of tuples representing the image data (width, height, prediction)
		val img_array = data.select("w", "h", "prediction")
			.rdd.map(f => (f.getAs[Int](0), f.getAs[Int](1), f.getAs[Int](2))).collect()

		// create new image of the same size
		val out = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)

		for (j <- 0 until img_array.length - 1) {
			// get the color tuple for the pixel's cluster
			val color_tuple = colors(img_array(j)._3)
			// set the pixel's color in the output image using the color tuple
			out.setRGB(img_array(j)._1, img_array(j)._2,
				new Color(color_tuple._3, color_tuple._2, color_tuple._1).getRGB)
		}
		// return the segmented image
		out
	}
}

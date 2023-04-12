package org.br4ve.trave1er
package segmentation

import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

import java.awt.Color
import java.awt.image.BufferedImage

class ImageSegmentation(k: Int = 17) extends IImageSegmentation{
	private val evaluator = new ClusteringEvaluator()

	private val kmeans = new KMeans()
		.setK(k)
		.setSeed(1L)
		.setFeaturesCol("features")

	def modelFit(data: DataFrame): KMeansModel = {
		kmeans.fit(data)
	}

	def transformData(data: DataFrame, model: KMeansModel): DataFrame = {
		model.transform(data)
			.select("w", "h", "b", "g", "r", "features", "prediction")
	}

	def evaluate(data: DataFrame) = {
		evaluator.evaluate(data)
	}

	def getSegmentedImage(data: DataFrame, width: Int, height: Int): BufferedImage = {
		var colors: Map[Int, (Int, Int, Int)] = Map[Int, (Int, Int, Int)]()

		for (i <- 0 until k) {
			val cluster = data.filter(col("prediction") === i).rdd
			if (!cluster.isEmpty) {
				val s = cluster.first
				colors += (i -> (s(2).asInstanceOf[Int], s(3).asInstanceOf[Int], s(4).asInstanceOf[Int]))
			}
		}

		val img_array = data.select("w", "h", "prediction")
			.rdd.map(f => (f.getAs[Int](0), f.getAs[Int](1), f.getAs[Int](2))).collect()

		// create new image of the same size
		val out = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)

		for (j <- 0 until img_array.length - 1) {
			val color_tuple = colors(img_array(j)._3)
			out.setRGB(img_array(j)._1, img_array(j)._2,
				new Color(color_tuple._3, color_tuple._2, color_tuple._1).getRGB)
		}

		out
	}
}

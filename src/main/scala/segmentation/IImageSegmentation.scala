package org.br4ve.trave1er
package segmentation

import org.apache.spark.ml.clustering.{BisectingKMeansModel}
import org.apache.spark.sql.DataFrame

import java.awt.image.BufferedImage

trait IImageSegmentation {
	def modelFit(data: DataFrame): BisectingKMeansModel

	def transformData(data: DataFrame, model: BisectingKMeansModel): DataFrame

	def getSegmentedImage(data: DataFrame, width: Int, height: Int): BufferedImage

}


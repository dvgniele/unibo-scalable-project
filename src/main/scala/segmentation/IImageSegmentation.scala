package org.br4ve.trave1er
package segmentation

import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.sql.DataFrame

import java.awt.image.BufferedImage

trait IImageSegmentation {
	def modelFit(data: DataFrame): KMeansModel

	def transformData(data: DataFrame, model: KMeansModel): DataFrame

	def getSegmentedImage(data: DataFrame, width: Int, height: Int): BufferedImage

	}



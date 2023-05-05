package org.br4ve.trave1er
package segmentation

import org.apache.spark.ml.clustering.BisectingKMeansModel
import org.apache.spark.sql.DataFrame

import java.awt.image.BufferedImage

trait IImageSegmentation {
	/**
	 * starts the KMeans model fit
	 * @param data dataframe to use to fit the model
	 * @return fitted model
	 */
	def modelFit(data: DataFrame): BisectingKMeansModel

	/**
	 * Transforms the data to be handled by the model
	 * @param data data to transform
	 * @param model model to use to transform the data
	 * @return data transofrmed to DataFrame
	 */
	def transformData(data: DataFrame, model: BisectingKMeansModel): DataFrame

	/**
	 * Retrieves the segmented image data
	 * @param data
	 * @param width
	 * @param height
	 * @return segmented image data
	 */
	def getSegmentedImage(data: DataFrame, width: Int, height: Int): BufferedImage

	}



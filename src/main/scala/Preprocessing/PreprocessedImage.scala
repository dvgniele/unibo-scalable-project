package org.br4ve.trave1er
package Preprocessing

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.functions.{col, collect_list}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable

/**
 * this singleton provides the method to preprocess images
 */
object PreprocessedImage {
	/***
	 * Transform the image dataframe in anew one with the color merged in a new column
	 * @param spark: current spark session
	 * @param dataFrame: image read as dataframe
	 * @return a Tuple that contains a new dataframe with rgb columns and the width/height information
	 */
	def decodeImageDataFrame(spark: SparkSession, dataFrame: DataFrame): (DataFrame, Int, Int) = {

		// transform the image matrix as a flat array
		val rdd_img_array = dataFrame.select(col("image.data"))
			.rdd.flatMap(f => f.getAs[Array[Byte]]("data"))

		// read image's metadata
		val img_data = dataFrame.select(col("image.*"))
			.rdd.map(row => (
			row.getAs[Int]("height"),
			row.getAs[Int]("width"),
			row.getAs[Int]("nChannels"),
			row.getAs[Byte]("data")))
			.collect()(0)

		val height = img_data._1
		val width = img_data._2
		val nChannels = img_data._3

		// Define accumulators for offset => to determine which color channel (B G R), x, and y
		var offSet = spark.sparkContext.longAccumulator("offSetAcc")
		var x = spark.sparkContext.longAccumulator("xAcc")
		var y = spark.sparkContext.longAccumulator("yAcc")
		x.add(1)
		y.add(1)

		import spark.implicits._
		// Convert the flat image array to a DataFrame of (color, w, h) tuples
		var final_image_df = rdd_img_array.zipWithIndex().map { f =>
			// Determine the offset based on the current index in the image array
			if (offSet.value == 0) {
				// Blue channel
				offSet.add(1)
				if (f._2 != 0)
					x.add(1)
			} else if (offSet.value == 1) {
				// Green channel
				offSet.add(1)
			} else if (offSet.value == 2) {
				// Red channel
				offSet.reset()
			}
			// Reset x and increment y when we've reached the end of a row
			if (x.value == width) {
				x.reset()
				y.add(1)
			}			
			// Return a tuple of (color, w, h) for this pixel	
			(f._1 & 0xFF, x.value, y.value)
		}.toDF
			.withColumnRenamed("_1", "color")
			.withColumnRenamed("_2", "w")
			.withColumnRenamed("_3", "h")
		// Group the DataFrame by (w, h) and collect colors into a list
		final_image_df = final_image_df.groupBy(col("w"), col("h"))
			.agg(collect_list(col("color")).as("color"))
			.orderBy(col("w"), col("h"))
			.rdd
			.map { f =>
			    // Convert the list of colors for each (w, h) group into separate columns for R, G, and B
				val a = f(2).asInstanceOf[mutable.WrappedArray[Int]]
				(f(0).toString.toInt, f(1).toString.toInt, a(0), a(1), a(2))
			}
			.toDF
			.withColumnRenamed("_1", "w")
			.withColumnRenamed("_2", "h")
			.withColumnRenamed("_3", "b")
			.withColumnRenamed("_4", "g")
			.withColumnRenamed("_5", "r")

		// extracting rgb channels features
		val features_col = Array(
			// "w","h",
			"b", "g", "r")
		
		// Create a new instance of VectorAssembler
		val vector_assembler = new VectorAssembler()
			.setInputCols(features_col) 	// Specify the input columns to the assembler
			.setOutputCol("features")		// Specify the output column name

		// Transform the DataFrame final_image_df using the VectorAssembler
		val va_transformed_df = vector_assembler.transform(final_image_df)

		// Return a Tuple3 object containing the transformed DataFrame and the image dimensions width and height
		new Tuple3[DataFrame, Int, Int](va_transformed_df, width, height)

	}
}

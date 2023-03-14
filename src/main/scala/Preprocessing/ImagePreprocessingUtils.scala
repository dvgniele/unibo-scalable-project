package org.br4ve.trave1er
package Preprocessing

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{col, collect_list}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.WrappedArray

class ImagePreprocessingUtils(dataframe: DataFrame, spark: SparkSession) extends Serializable {
	private val sc = spark.sparkContext

	private val imageRDD = df2RDD(dataframe)

	private val image_data = dataframe.select(col("image.*"))
		.rdd.map(row => (
		row.getAs[Int]("height"),
		row.getAs[Int]("width"),
		row.getAs[Int]("nChannels"),
		row.getAs[Byte]("data")
	))
		.collect()(0)

	private val height = image_data._1
	private val width = image_data._2
	private val nChannels = image_data._3

	private val offSet = sc.longAccumulator("offSetAcc")
	private val x = sc.longAccumulator("xAcc")
	private val y = sc.longAccumulator("yAcc")
	x.add(1)
	y.add(1)

	def getWidth: Int =  width
	def getHeight: Int =  height

	def df2RDD(data: DataFrame): RDD[Byte] = {
		data.select(col("image.data"))
			.rdd.flatMap(f => f.getAs[Array[Byte]]("data"))
	}

	def getPreprocessedDataFrame: DataFrame = {
		// extracting information about channels height, width
		import spark.implicits._
		var final_image_df = imageRDD.zipWithIndex().map { f =>
			if (offSet.value == 0) {
				// b
				offSet.add(1)
				if (f._2 != 0)
					x.add(1)
			} else if (offSet.value == 1) {
				//g
				offSet.add(1)
			} else if (offSet.value == 2) {
				//r
				offSet.reset()
			}
			if (x.value == width) {
				x.reset()
				y.add(1)
			}

			(f._1 & 0xFF, x.value, y.value)
		}.toDF
			.withColumnRenamed("_1", "color")
			.withColumnRenamed("_2", "w")
			.withColumnRenamed("_3", "h")

		final_image_df = final_image_df.groupBy(col("w"), col("h"))
			.agg(collect_list(col("color")).as("color"))
			.orderBy(col("w"), col("h"))
			.rdd
			.map { f =>
				val a = f(2).asInstanceOf[WrappedArray[Int]]
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
		val vector_assembler = new VectorAssembler()
			.setInputCols(features_col)
			.setOutputCol("features")

		val va_transformed_df = vector_assembler.transform(final_image_df)

		va_transformed_df
	}
}
package org.br4ve.trave1er
package utils

import java.awt.image.BufferedImage

object Utils {
	def image2PixelMatrix(image: BufferedImage): Array[Double] = {
		val width = image.getWidth
		val height = image.getHeight
		val pixelMatrix = Array.ofDim[Double](height, width)

		for (y <- 0 until height) {
			for (x <- 0 until width) {
				val pixel = image.getRGB(x, y)
				/*
				val red = (pixel >> 16) & 0xff
				val green = (pixel >> 8) & 0xff
				val blue = pixel & 0xff
				val gray = (0.2989 * red + 0.5870 * green + 0.1140 * blue) / 255.0
				*/
				pixelMatrix(y)(x) = pixel & 0xffffff //  remove alpha channel
			}
		}

		// Flatten the pixel matrix into a vector
		pixelMatrix.flatten
	}

	def images2PixelMatrix(images: Array[BufferedImage]): Array[Array[Double]] = {
		var array_of_matrices = new Array[Array[Double]](0)
		images.foreach(x => {
			val pixelMatrix = image2PixelMatrix(x)
			array_of_matrices :+= pixelMatrix
		})

		array_of_matrices
	}
}

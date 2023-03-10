package org.br4ve.trave1er
package Segmentation

import org.apache.spark.ml.clustering.KMeans

class ImageSegmentation {
  private val numberCluster = 5
  private val kmeans = new KMeans()
    .setK(numberCluster)
    .setSeed(1L)

}

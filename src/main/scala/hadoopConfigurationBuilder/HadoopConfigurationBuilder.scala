package org.br4ve.trave1er
package hadoopConfigurationBuilder

import org.apache.hadoop.conf.Configuration

/**
 * this singleton provides the configuration object for hadoop, both for local and cloud
 */
object HadoopConfigurationBuilder {

  private val remoteSource = s"gs://brave-bucket/dataset";
  private val localSource = s"./dataset/dataset"

  /**
   *
   * @return
   */
  def getHadoopConfigurationForGoogleCloudPlatform: Configuration = {
    val jsonKeyFilePath = "/home/notty/key.json"
    val hadoopConfiguration = new Configuration()
    hadoopConfiguration.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    hadoopConfiguration.set("fs.gs.project.id", "helloworld-379211")
    hadoopConfiguration.set("fs.gs.system.bucket", "brave-bucket")
    hadoopConfiguration.set("google.cloud.auth.service.account.enable", "true")
    hadoopConfiguration.set("google.cloud.auth.service.account.json.keyfile", jsonKeyFilePath)
    hadoopConfiguration.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    hadoopConfiguration.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    hadoopConfiguration.set("spark.hadoop.fs.gs.working.dir", remoteSource)
    hadoopConfiguration
  }

  /**
   *
   * @return
   */
  def getHadoopConfigurationForLocalStorage: Configuration = {
    new Configuration()
  }

  /**
   *
   * @return
   */

  def getLocalSource: String = localSource

  /**
   *
   * @return
   */
  def getRemoteSource: String = remoteSource

}

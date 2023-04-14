package org.br4ve.trave1er
package hadoopConfigurationBuilder

import org.apache.hadoop.conf.Configuration

object HadoopConfigurationBuilder {

  private val remoteSource = s"gs://brave-bucket-d/dataset";
  private val localSource = s"./dataset/dataset"

  def getHadoopConfigurationForGoogleCloudPlatform: Configuration = {
    val jsonKeyFilePath = "/home/ktm17/config/key.json"
    val hadoopConfiguration = new Configuration()
    hadoopConfiguration.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    hadoopConfiguration.set("fs.gs.project.id", "scala-unibo")
    hadoopConfiguration.set("fs.gs.system.bucket", "brave-bucket-d")
    hadoopConfiguration.set("google.cloud.auth.service.account.enable", "true")
    hadoopConfiguration.set("google.cloud.auth.service.account.json.keyfile", jsonKeyFilePath)
    hadoopConfiguration.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    hadoopConfiguration.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    hadoopConfiguration.set("spark.hadoop.fs.gs.working.dir", remoteSource)
    hadoopConfiguration
  }

  def getHadoopConfigurationForLocalStorage: Configuration = {
    new Configuration()
  }

  def getLocalSource: String = localSource

  def getRemoteSource: String = remoteSource

}

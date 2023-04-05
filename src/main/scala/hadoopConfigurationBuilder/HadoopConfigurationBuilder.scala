package org.br4ve.trave1er
package hadoopConfigurationBuilder

import org.apache.hadoop.conf.Configuration

object HadoopConfigurationBuilder {

  private val remoteSource = s"gs://br4ve-trave1er/dataset";
  private val localSource = s"./dataset/dataset"

  def getHadoopConfigurationForGoogleCloudPlatform: Configuration = {
    val jsonKeyFilePath = "/home/notty/config/helloworld-379211-1a6eefa37a81.json"
    val hadoopConfiguration = new Configuration()
    hadoopConfiguration.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    hadoopConfiguration.set("fs.gs.project.id", "helloworld-379211")
    hadoopConfiguration.set("fs.gs.system.bucket", "br4ve-trave1er")
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

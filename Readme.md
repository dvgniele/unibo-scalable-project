# Scalable Project 2022/23

Our project provides an implementation of color segmentation using kmeans algorithm on a dataset of images.

The application can be run on either a cloud or a local machine.
The following sections describe how to run it.

## Prerequisites

* JDK 11
* Scala plugin installed (2.13.8)
* [Google SDK](https://cloud.google.com/sdk/docs/install)

### Prerequisites for local execution

* Hadoop 3.3.0 available at the following [link](https://liveunibo-my.sharepoint.com/:u:/g/personal/stefano_notari2_studio_unibo_it/EfnKa80vvUpElSofKFkpXCYBAmJp23vD6zxiJG8-69XR0w?e=siE2iT)

#### Hadoop Configuration

The complete procedure to install hadoop is available at the following [link](https://kontext.tech/article/447/install-hadoop-330-on-windows-10-step-by-step-guide).
The most important things to do are the following:

* add the system variable of JAVA_HOME, the root directory of JDK without the bin subfolder (C:\Users\user\.jdks\corretto-11.0.18) 
* add the bin folder of the JDK to the PATH environment variable (C:\Users\user\.jdks\corretto-11.0.18\bin)
* add the system variable HADOOP_HOME, the root directory of the folder without the bin subfolder (C:\Users\user\Documents\lib\hadoop-3.3.0\bin)
* add the bin folder of hadoop to the path environment variable (C:\Users\user\Documents\lib\hadoop-3.3.0\bin)

## Set up of Google Cloud Platform

* Create a new project
* Link a billing account to it

### Automatically deploy the application through bat script

We wrote a script to deploy automatically the application on the cloud, you can find it at `scripts/setupCloudEnvironment.bat`.
Before executing it, you need to set:

* PROJECT_ID
* CLUSTER_NAME
* BUCKET_NAME
* SA_NAME (Service Account)

Then, create a folder named `resources` inside the `scripts` folder and copy the fat jar of the application and the dataset to upload.

Finally, move to the `scripts` folder and run the following command

```
./setupCloudEnvironment.bat
```

### Set up of Google Cloud Storage

To set up a bucket use the following command, remember to set the bucket name and the project id

```
gcloud storage buckets create gs://<BUCKET_NAME> --project=<PROJECT_ID> --default-storage-class=Standard --location=eu --uniform-bucket-level-access
```

After that, it is necessary to create a service account with the permission to read and write file to the bucket.

```
gcloud iam service-accounts create <SA_NAME> --description="service account for Google Cloud Storage" --display-name="DISPLAY_NAME"
```

Once created the new service account is possible to bind it with the project and add the permission to read/write on the bucket.
```
gcloud projects add-iam-policy-binding PROJECT_ID --member="serviceAccount:SA_NAME@PROJECT_ID.iam.gserviceaccount.com" --role="roles/storage.admin"
```

After that, it is possible to create the keyfile necessary to authenticate the account from the application
```
gcloud iam service-accounts keys create PATH_KEY_FILE\FILE_NAME.json --iam-account=SA_NAME@PROJECT_ID.iam.gserviceaccount.com
```

Finally, add the file to the `config` folder and remember to update the following variables in `src/main/scala/hadoopConfigurationBuilder/HadoopConfigurationBuilder.scala`:

* line 8: the bucket name where the data are
* line 12: the absolute path to the key file on the cluster
* line 15: the id of project
* line 16: the name of the bucket where the data are

### set up of cluster with Dataproc

The following command create a new cluster.

```
gcloud dataproc clusters create CLUSTER_NAME --enable-component-gateway --bucket BRAVE_BUCKET --region europe-west1 --zone europe-west1-b --single-node --master-machine-type n1-custom-CORES-MEMORY --master-boot-disk-size 500 --image-version 2.1-debian11 --project PROJECT_ID
```
After the creation is possible to start the cluster

```
gcloud dataproc clusters start CLUSTER_NAME  --region=europe-west1
```

### last setup necessary to run a job on cluster

Since the application needs to read/write file on the bucket, it needs the json file with the key for authentication.
So it is necessary to copy the file on the cluster with the following command:
```
gcloud compute scp FILE_NAME.json CLUSTER-NAME-m:/ABS_PATH_TO_USR_FOLDER/config --zone=europe-west1-b
```

After that is necessary to upload the jar on the bucket.

## START JOB

```
gcloud dataproc jobs submit spark --class org.br4ve.trave1er.Main --jars gs://BUCKET_NAME/my-application.jar --cluster CLUSTER_NAME --region europe-west1 \
    --properties="spark.jars.packages=Microsoft:spark-images:0.1,spark.hadoop.google.cloud.auth.service.account.enable=true,spark.hadoop.google.cloud.auth.service.account.json.keyfile=/ABS_PATH_TO_USR_FOLDER/config/FILE_NAME.json,spark.hadoop.fs.gs.project.id=PROJECT_ID,spark.eventLog.enabled=false"
```

## Contributors

Stefano Notari

Daniele Perrella

Francesco Santilli
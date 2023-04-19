@ECHO off
echo Starting the script....

::set /p PROJECT_ID="Insert the project id: "
::set /p CLUSTER_NAME="Insert the name of the cluster: "
::set /p BUCKET_NAME="Insert the name of bucket: "
::set /p SA_NAME="Insert the name of service account: "

set PROJECT_ID=helloworld-379211
set CLUSTER_NAME=brave-cluster-5k
set BUCKET_NAME=brave-bucket
set SA_NAME=servicetestscript

echo Creating the bucket...

set STORAGE=Standard
set LOCATION=eu

call gcloud storage buckets create gs://%BUCKET_NAME% --project=%PROJECT_ID% --default-storage-class=%STORAGE% --location=%LOCATION% --uniform-bucket-level-access

echo Creating the service account...

set SA_DESCRIPTION="service account for Google Cloud Storage"
set SA_DISPLAY_NAME="DISPLAY NAME"

call gcloud iam service-accounts create %SA_NAME% --description=%SA_DESCRIPTION% --display-name=%SA_DISPLAY_NAME%

echo Binding the service account to the bucket

set MEMBER="serviceAccount:%SA_NAME%@%PROJECT_ID%.iam.gserviceaccount.com"
set ROLE="roles/storage.admin"

call gcloud projects add-iam-policy-binding %PROJECT_ID% --member=%MEMBER% --role=%ROLE%

echo Downloading the json key file...

set FOLDER=./resources
set FILENAME=key.json

set KEY_FILE=%FOLDER%/%FILENAME%

call gcloud iam service-accounts keys create %KEY_FILE% --iam-account=%SA_NAME%@%PROJECT_ID%.iam.gserviceaccount.com

echo Creating the Cluster...

set REGION=europe-west1
set ZONE=europe-west1-b
set CORES=24
set MEMORY=64000

call gcloud dataproc clusters create %CLUSTER_NAME% --enable-component-gateway --bucket %BUCKET_NAME% --region %REGION% --zone %ZONE% --single-node --master-machine-type n1-custom-%CORES%-%MEMORY% --master-boot-disk-size 500 --image-version 2.1-debian11 --project %PROJECT_ID%

echo Uploading the key

set CLUSTER_PATH=/home/notty

call gcloud compute scp %KEY_FILE% %CLUSTER_NAME%-m:%CLUSTER_PATH% --zone=%ZONE%

echo Uploading the jar and the key file on the bucket

set JAR_NAME=my-application.jar
set JAR_PATH=%FOLDER%/%JAR_NAME%
set DATASET_PATH=%FOLDER%/dataset

call gsutil cp %KEY_FILE% gs://%BUCKET_NAME%/
call gsutil cp %JAR_PATH% gs://%BUCKET_NAME%/
call gsutil -m cp -R %DATASET_PATH% gs://%BUCKET_NAME%/

echo upload the dataset on the bucket

PAUSE

echo Starting the JOB

set JAR_MAIN_CLASS=org.br4ve.trave1er.Main

call gcloud dataproc jobs submit spark --class %JAR_MAIN_CLASS% --jars gs://%BUCKET_NAME%/%JAR_NAME% --cluster %CLUSTER_NAME% --region %REGION% --properties="spark.jars.packages=Microsoft:spark-images:0.1,spark.hadoop.google.cloud.auth.service.account.enable=true,spark.hadoop.google.cloud.auth.service.account.json.keyfile=%CLUSTER_PATH%/%FILENAME%,spark.hadoop.fs.gs.project.id=%PROJECT_ID%,spark.eventLog.enabled=false,spark.driver.memory=8g" --executor-memory 32g --driver-memory 32g

PAUSE
# Scalable Project 2022/23

## Prerequisites

* JDK 11
* Scala plugin installed (2.13.8)
* [Google SDK](https://cloud.google.com/sdk/docs/install)
* Hadoop 3.3.0 available at the following [link](https://liveunibo-my.sharepoint.com/:u:/g/personal/stefano_notari2_studio_unibo_it/EfnKa80vvUpElSofKFkpXCYBAmJp23vD6zxiJG8-69XR0w?e=siE2iT)

### Hadoop Configuration

The complete procedure to install hadoop is available at the following [link](https://kontext.tech/article/447/install-hadoop-330-on-windows-10-step-by-step-guide).
The most important thing to do are the following:

* add the environment variable of JAVA_HOME, the root directory of JDK without the bin subfolder (C:\Users\user\.jdks\corretto-11.0.18) 
* add the bin folder of the JDK to the path environment variable (C:\Users\user\.jdks\corretto-11.0.18\bin)
* add the environment variable HADOOP_HOME, the root directory of the folder without the bin subfolder (C:\Users\user\Documents\lib\hadoop-3.3.0\bin)
* add the bin folder of hadoop to the path environment variable (C:\Users\user\Documents\lib\hadoop-3.3.0\bin)

# How to deploy the application on Container Registry

In order to run the application as a Job in google cloud you first need to build the Dockerfile.

In the root folder of the application run the following command (replacing the token {tag_image} with the desired tag)

```docker build -t {tag_image} .```

After that you need to tag the created image with the address of google Cloud Registry, this can be done with the following command

```docker tag {tag_image} gcr.io/{Project_id}/{tag_image}:{tag_image_on_gcr}```

Finally, it's possible to push the image with the command

```docker push gcr.io/{Project_id}/{tag_image}:{tag_image_on_gcr}```

## Image Segmentation

## Contributors

Stefano Notari
Daniele Perrella
Francesco Santilli
# Scalable Project 2022/23

## Prerequisites

* JDK 17
* Scala plugin installed
* [Google SDK](https://cloud.google.com/sdk/docs/install)

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
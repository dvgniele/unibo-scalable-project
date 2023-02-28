FROM openjdk:17-jdk-slim

RUN apt update -y
RUN apt install unzip -y
RUN apt install curl -y

ENV SBT_VERSION 1.8.2

RUN curl -L -o sbt-$SBT_VERSION.zip https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.zip
RUN unzip sbt-$SBT_VERSION.zip -d ops

WORKDIR /br4ve-trave1er

ADD . /br4ve-trave1er
CMD /ops/sbt/bin/sbt run
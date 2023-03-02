FROM sbtscala/scala-sbt:eclipse-temurin-17.0.2_1.6.2_2.13.8

RUN git clone https://github.com/dvgniele/unibo-scalable-project.git /br4ve-trave1er

WORKDIR /br4ve-trave1er

ENTRYPOINT ["sbt", "run"]
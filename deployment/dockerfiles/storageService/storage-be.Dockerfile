#Step-1
FROM gradle:7.4.1-jdk17 AS TEMP_BUILD_IMAGE
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

#Step-2
FROM openjdk:17-jdk
ENV ARTIFACT_NAME=storage-lib-1.0.0.jar

#Install Minio Client
# WORKDIR /usr/local/bin/
# RUN sudo wget https://dl.min.io/client/mc/release/linux-amd64/mc 

COPY ./mc /usr/local/bin/
RUN sudo chmod +x /usr/local/bin/mc

USER 1000
COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/storage-lib/build/libs/$ARTIFACT_NAME $ARTIFACT_NAME

EXPOSE 7175
CMD java -jar $ARTIFACT_NAME
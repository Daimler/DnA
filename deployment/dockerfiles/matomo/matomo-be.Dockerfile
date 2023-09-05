#Step-1
FROM gradle:7.4.1-jdk17 AS TEMP_BUILD_IMAGE
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

#Step-2
FROM openjdk:17-jdk
ENV ARTIFACT_NAME=data-product-lib-1.0.0.jar
USER 1000
COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/data-product-lib/build/libs/$ARTIFACT_NAME $ARTIFACT_NAME

EXPOSE 8989
CMD java -jar $ARTIFACT_NAME
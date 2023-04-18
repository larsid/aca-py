FROM maven:3.9.0 as build-stage
ADD . .
RUN mvn compile && mvn package

FROM ubuntu:20.04
COPY --from=build-stage target/soft-iot-dlt-aca-py-1.0-jar-with-dependencies.jar soft-iot-dlt-aca-py-1.0-jar-with-dependencies.jar
RUN apt-get update && apt-get install -y net-tools \
    iputils-ping \
    iproute2  \
    openjdk-11-jdk
RUN export DISPLAY=unix:0.0
CMD /bin/bash
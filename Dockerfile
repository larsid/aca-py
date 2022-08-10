FROM openjdk:11
ADD target/aca-py-1.0.jar aca-py-1.0.jar
ENTRYPOINT ["java", "-jar","aca-py-1.0.jar"]
EXPOSE 1883
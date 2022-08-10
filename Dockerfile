FROM openjdk:11
ADD target/aca-py-1.0-SNAPSHOT.jar aca-py-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar","aca-py-1.0-SNAPSHOT.jar"]
EXPOSE 1883
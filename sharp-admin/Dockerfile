# For Java 8, try this
# FROM openjdk:8-jdk-alpine

# For Java 8, try this
FROM openjdk:8-jdk-alpine

# Refer to Maven build -> finalName
ARG JAR_FILE=target/sharp-admin-2.0-SNAPSHOT.jar

# cd /opt/app
WORKDIR /opt/app

# cp target/sharp-admin-2.0-SNAPSHOT.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker-prod","-jar","app.jar"]
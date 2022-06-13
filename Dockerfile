FROM openjdk:11

ARG JAR_FILE=target/Application-jar-with-dependencies.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar","production"]
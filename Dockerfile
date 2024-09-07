#FROM openjdk:11
#ARG JAR_FILE=target/Application-jar-with-dependencies.jar
#COPY ${JAR_FILE} app.jar
#ENV TZ=America/New_York
#RUN ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && dpkg-reconfigure -f noninteractive tzdata
#ENTRYPOINT ["java","-jar","/app.jar","production"]

FROM openjdk:11

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package

# Argument for JAR file
ARG JAR_FILE=target/Application-jar-with-dependencies.jar

# Copy the built JAR to the container
COPY ${JAR_FILE} app.jar

# Set timezone
ENV TZ=America/New_York
RUN ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

# Entry point to run the application
ENTRYPOINT ["java", "-jar", "/app.jar", "production"]
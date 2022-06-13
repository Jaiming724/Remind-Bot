FROM openjdk:11

ARG JAR_FILE=target/Application-jar-with-dependencies.jar
COPY ${JAR_FILE} app.jar
ENV TZ=America/New_York
RUN ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && dpkg-reconfigure -f noninteractive tzdata
ENTRYPOINT ["java","-jar","/app.jar","production"]
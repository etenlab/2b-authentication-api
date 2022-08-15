FROM arm64v8/amazoncorretto:18
ARG JAR_FILE=target/*.jar
COPY build/libs/authentication-0.0.1-SNAPSHOT.jar app.jar
COPY build/resources/main/application.yml application.yml
ENTRYPOINT ["java","-jar","/app.jar"]

FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests -pl common,server -am

FROM amazoncorretto:21-alpine
WORKDIR /app

# Set environment variables
ENV XML_LOAD="file.xml"
COPY --from=build /app/build/moviemanager-server.jar app.jar
EXPOSE 7878
ENTRYPOINT ["java", "-jar", "app.jar"]
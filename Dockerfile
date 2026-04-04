FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM amazoncorretto:17-alpine
WORKDIR /app

# Set environment variables
ENV XML_LOAD="file.xml"
RUN touch /app/file.xml && chmod 666 /app/file.xml
COPY --from=build /app/build/moviemanager-server.jar app.jar
EXPOSE 7878
ENTRYPOINT ["java", "-jar", "app.jar"]
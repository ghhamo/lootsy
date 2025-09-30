FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd -ms /bin/bash appuser && mkdir -p /app/storage && chown -R appuser:appuser /app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER appuser

ENV IMAGE_FOLDER=/app/storage
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

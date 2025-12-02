# FROM openjdk:21
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY target/astrologer-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
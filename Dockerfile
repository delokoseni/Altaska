#Этап сборки
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#Этап запуска
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/Altaska-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
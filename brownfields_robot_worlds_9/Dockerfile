FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/robot-world-0.0.2.jar app.jar
COPY config.json ./config.json

EXPOSE 5050

CMD ["java", "-jar", "app.jar"]

# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Load env variables (used in application.properties)
ENV SPRING_PROFILES_ACTIVE=default

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

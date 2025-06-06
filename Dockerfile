# Stage 1: Build the application using Gradle
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

RUN chmod +x ./gradlew

COPY src src

RUN ./gradlew bootJar


FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the application with production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

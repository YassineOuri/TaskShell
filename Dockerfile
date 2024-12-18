# Use official OpenJDK 17 image as base
FROM openjdk:17-jdk-slim

# Set working directory in the container
WORKDIR /app

# Copy the Maven project's JAR file into the container
COPY target/*.jar app.jar

# Set the entrypoint to run the Spring Shell JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
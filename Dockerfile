# Use an official OpenJDK runtime as a parent image
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the parent POM and module directories
COPY pom.xml ./
COPY json2sql-lib ./json2sql-lib
COPY translator-app ./translator-app

# Build the project
RUN mvn clean install -DskipTests

# Use a smaller runtime image for the final build
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file of the translator-app
COPY --from=build /app/translator-app/target/translator-app-*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

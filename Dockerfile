# Use OpenJDK base image with Maven installed
FROM maven:3.9.6-eclipse-temurin-17

# Set working directory
WORKDIR /app

# Copy everything into the container
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Expose the port (Spring Boot will use this)
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "target/VoipCallBackend-0.0.1-SNAPSHOT.jar"]

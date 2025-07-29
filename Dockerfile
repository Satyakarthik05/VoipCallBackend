# Use OpenJDK with Maven
FROM maven:3.9.6-eclipse-temurin-17

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the app (skip tests for speed)
RUN mvn clean package -DskipTests

# Expose dynamic port
EXPOSE 8080

# Run the correct JAR file
CMD ["java", "-jar", "target/VoipCall-0.0.1-SNAPSHOT.jar"]

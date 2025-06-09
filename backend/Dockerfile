# Backend Dockerfile for Spring Boot Time Tracker API (Java 21)
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy POM file first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r timetracker && useradd -r -g timetracker timetracker

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown timetracker:timetracker app.jar

# Switch to non-root user
USER timetracker

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
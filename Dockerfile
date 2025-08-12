# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Create directory for file uploads
RUN mkdir -p /tmp/uploads && \
    chmod 777 /tmp/uploads

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV DATABASE_URL=jdbc:postgresql://localhost:5432/travel
ENV DATABASE_USERNAME=postgres
ENV DATABASE_PASSWORD=postgres
ENV FILE_UPLOAD_DIRECTORY=/tmp/uploads
ENV SECRECT_JWT_KEY=defaultkey

# Run the application
CMD ["java", "-jar", "app.jar"] 
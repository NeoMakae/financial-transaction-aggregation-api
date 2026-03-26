# Use a lightweight OpenJDK image
FROM eclipse-temurin:17-jdk-alpine

# Set work directory
WORKDIR /app

# Copy Maven build output
COPY target/financial-transaction-aggregation-api-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
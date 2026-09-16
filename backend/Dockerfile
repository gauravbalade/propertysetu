FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/property-registration-backend-0.0.1.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

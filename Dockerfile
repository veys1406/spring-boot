FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw package -DskipTests
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=builder /app/target/spboot-0.0.1-SNAPSHOT.jar app.jar
RUN useradd appuser
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
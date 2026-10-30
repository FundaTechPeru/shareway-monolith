# syntax=docker/dockerfile:1

# Build stage: compiles and packages the monolith with the Maven wrapper on JDK 25.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Runtime stage: only the JRE, curl for the health check and the executable jar, run by an
# unprivileged user.
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system shareway \
    && useradd --system --gid shareway --home-dir /app shareway \
    && mkdir -p /app/storage \
    && chown shareway:shareway /app/storage
COPY --from=build /workspace/target/shareway-monolith-*.jar /app/shareway-monolith.jar
USER shareway
ENV SERVER_PORT=8080 \
    STORAGE_PATH=/app/storage
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -fs "http://localhost:${SERVER_PORT}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "/app/shareway-monolith.jar"]

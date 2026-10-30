# syntax=docker/dockerfile:1

# Build stage: compiles and packages the monolith with the Maven wrapper on JDK 25.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Runtime stage: only the JRE and the executable jar, run by an unprivileged user.
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system shareway \
    && useradd --system --gid shareway --home-dir /app shareway
COPY --from=build /workspace/target/shareway-monolith-*.jar /app/shareway-monolith.jar
USER shareway
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/shareway-monolith.jar"]

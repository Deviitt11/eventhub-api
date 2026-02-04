# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy only what Gradle needs first (better caching)
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./
COPY src ./src

# Build executable jar
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl for healthchecks (Compose + Docker)
RUN apt-get update && apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

# Non-root user
RUN useradd -r -u 10001 appuser
USER appuser

# Copy jar (avoid assuming jar name)
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

# Allow passing JVM opts (optional)
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
# ---- Build stage -----------------------------------------------------------
# Uses the Maven wrapper so the image builds with the exact Maven version
# pinned in .mvn/wrapper/maven-wrapper.properties, matching local dev.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# The bare JDK Alpine image ships neither ca-certificates nor curl, which
# makes the wrapper's HTTPS download of the Maven distribution fail (or
# hang indefinitely under busybox wget) -- install them explicitly first.
RUN apk add --no-cache curl ca-certificates

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src ./src
RUN ./mvnw -q -B clean package -DskipTests

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Runs as a non-root user per OWASP container hardening guidance.
RUN addgroup -S scholarshiphub && adduser -S scholarshiphub -G scholarshiphub
WORKDIR /app

COPY --from=build /workspace/target/scholarshiphub-backend.jar app.jar
RUN mkdir -p /app/uploads /app/logs && chown -R scholarshiphub:scholarshiphub /app

USER scholarshiphub
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

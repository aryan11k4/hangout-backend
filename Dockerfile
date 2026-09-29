# ---------- Build stage ----------
# Uses a full JDK + Maven image, only to compile the app - this whole
# stage (and everything it downloads) is discarded and never shipped in
# the final image, it just produces the JAR.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only the files needed to resolve dependencies first, so Docker can
# cache this layer - dependencies only get re-downloaded when pom.xml
# actually changes, not on every source code edit.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Now copy the actual source and build. Skips tests during the Docker
# build itself (-DskipTests) - Render's build step is not where you want
# a test suite running; run tests in CI before merging, not on every deploy.
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---------- Run stage ----------
# Slim JRE-only image (no JDK, no Maven, no build cache) - this is what
# actually gets deployed and run by Render.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built JAR out of the build stage.
COPY --from=build /app/target/*.jar app.jar

# Render sets $PORT at runtime and expects the app to bind to it - your
# application.yml already reads server.port from ${SERVER_PORT:8080}, so
# this is overridden by setting SERVER_PORT=$PORT below rather than
# hardcoding 8080. Render injects PORT automatically; you don't set it
# yourself in the dashboard.
EXPOSE 8080

# Runs the JAR, passing Render's PORT through to your existing
# SERVER_PORT property. sh -c is required here so $PORT actually expands -
# a plain exec-form ENTRYPOINT would pass the literal string "$PORT"
# instead of its value.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]

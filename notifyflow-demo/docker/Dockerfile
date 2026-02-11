# ============================================
# NotifyFlow Nova - Multi-stage Docker Build
# ============================================
# Stage 1: Build with Maven + JDK 21
# Stage 2: Lightweight runtime with JRE 21
# ============================================

# --- Build Stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and POM files first (leverage Docker cache)
COPY pom.xml ./
COPY notifyflow-core/pom.xml notifyflow-core/
COPY notifyflow-demo/pom.xml notifyflow-demo/

# Copy source code
COPY notifyflow-core/src notifyflow-core/src
COPY notifyflow-demo/src notifyflow-demo/src

# Install Maven (Alpine doesn't have it by default)
RUN apk add --no-cache maven

# Build the project
RUN mvn clean package -DskipTests -q

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the fat JAR from build stage
COPY --from=build /app/notifyflow-demo/target/notifyflow-demo.jar app.jar

# Default demo mode
ENV DEMO_MODE=all

# Run the demo
ENTRYPOINT ["sh", "-c", "java -jar app.jar ${DEMO_MODE}"]

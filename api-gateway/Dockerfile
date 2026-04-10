# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle .

# Корневой build.gradle содержит GraalVM plugin, не нужный при Docker-сборке.
# Каждый субмодуль имеет свой build.gradle — заменяем корневой минимальной версией.
RUN echo '' > build.gradle

COPY common/build.gradle       common/
COPY auth-service/build.gradle auth-service/
COPY api-gateway/build.gradle  api-gateway/

RUN chmod +x gradlew

# Зависимости (кешируется пока build.gradle не меняется)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api-gateway:dependencies --no-daemon -q

# Исходники
COPY common/src       common/src
COPY api-gateway/src  api-gateway/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api-gateway:bootJar --no-daemon -x test

# ─── Stage 2: Extract layers ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS extractor
WORKDIR /app

COPY --from=builder /workspace/api-gateway/build/libs/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

# ─── Stage 3: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=extractor /app/dependencies/          ./
COPY --from=extractor /app/spring-boot-loader/    ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/           ./

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]

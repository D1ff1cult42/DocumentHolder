FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle ./
COPY gradle ./gradle
COPY common/build.gradle      common/
COPY auth-service/build.gradle auth-service/
COPY api-gateway/build.gradle  api-gateway/

RUN chmod +x gradlew && ./gradlew :auth-service:dependencies --no-daemon -q

COPY common/src       common/src
COPY auth-service/src auth-service/src

RUN ./gradlew :auth-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/auth-service/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# ====== Build stage ======
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
# 若用 Flyway，讓它跟著 Spring 啟動跑 migration（預設行為）
RUN mvn -q -DskipTests package

# ====== Runtime stage ======
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -m appuser
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
# 健康檢查（可依實際健康端點調整）
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

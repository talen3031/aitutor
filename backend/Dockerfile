# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q clean -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -m appuser
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
#ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
#ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms64m -Xmx256m"
#ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms16m -Xmx64m"
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms16m -Xmx64m -XX:+UseSerialGC -Xss256k -XX:ReservedCodeCacheSize=24m -XX:MaxMetaspaceSize=64m -XX:MaxDirectMemorySize=16m -XX:+AlwaysActAsServerClassMachine -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

USER appuser
ENTRYPOINT exec java \
  -DOPENAI_API_KEY=${OPENAI_API_KEY} \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local} \
  $JAVA_OPTS -jar /app/app.jar
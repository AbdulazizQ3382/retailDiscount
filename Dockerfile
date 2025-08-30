FROM maven:3.9.6-eclipse-temurin-21 AS test
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

COPY sonar-project.properties ./sonar-project.properties

RUN mvn test

FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY --from=test /root/.m2 /root/.m2

COPY pom.xml .
COPY src ./src
COPY sonar-project.properties ./sonar-project.properties

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine AS runtime

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
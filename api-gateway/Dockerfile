# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Cache dependencies before copying sources for faster rebuilds.
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
LABEL maintainer="Janvier NTWALI HABIYAREMYE <janvierntwali@gmail.com>"
WORKDIR /app
COPY --from=build /build/target/api-gateway.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

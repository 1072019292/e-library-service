FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system --gid 10001 elibrary \
    && useradd --system --uid 10001 --gid elibrary --home-dir /app elibrary \
    && install -d -o elibrary -g elibrary /app/data
WORKDIR /app
COPY --from=build --chown=elibrary:elibrary /workspace/target/e-library-service-1.0.0.jar app.jar
USER elibrary
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

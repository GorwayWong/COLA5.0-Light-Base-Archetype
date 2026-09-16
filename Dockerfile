# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21.0.12_8-jdk-jammy AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY shared shared
COPY agent agent
COPY bootstrap bootstrap
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -pl bootstrap -am -DskipTests package

FROM build AS test

CMD ["./mvnw", "-B", "-pl", "bootstrap", "-am", "verify", "-Pintegration"]

FROM eclipse-temurin:21.0.12_8-jre-jammy AS runtime

USER root
RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home --shell /usr/sbin/nologin tolink

WORKDIR /app
COPY --from=build /workspace/bootstrap/target/tolink-bootstrap-0.1.0-SNAPSHOT.jar /app/app.jar

USER tolink
STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]

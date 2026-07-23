FROM gradle:8.14.5-jdk17 AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootWar --no-daemon

FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system --gid 10001 bandi \
    && useradd --system --uid 10001 --gid bandi --create-home \
        --home-dir /app bandi \
    && mkdir -p /data/bandi \
    && chown -R bandi:bandi /app /data/bandi

WORKDIR /app

COPY --from=build --chown=bandi:bandi /workspace/build/libs/*.war /app/bandi.war

USER bandi

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/bandi.war"]

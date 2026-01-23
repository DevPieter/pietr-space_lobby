FROM eclipse-temurin:25-jdk
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/

COPY gradlew /app/
COPY gradle /app/gradle

COPY src /app/src

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon shadowJar

EXPOSE 25565

ENV APP_CONFIG_PATH=/app/data/config.json
ENV WORLD_DATA_PATH=/app/data/worlds

CMD ["java", "-jar", "build/libs/PietrSpaceLobby-1.1.0.jar"]
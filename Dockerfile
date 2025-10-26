FROM openjdk:25-jdk-slim
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/

COPY gradlew /app/
COPY gradle /app/gradle

COPY src /app/src

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon shadowJar

EXPOSE 25565

ENV WORLD_PATH=/app/world

CMD ["java", "-jar", "build/libs/PietrSpaceLobby-1.1.0.jar"]
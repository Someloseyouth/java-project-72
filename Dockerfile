FROM eclipse-temurin:21-jdk

WORKDIR /app

# Копируем Gradle wrapper и конфиги из подпапки app
COPY app/gradle gradle
COPY app/build.gradle.kts .
COPY app/settings.gradle.kts .
COPY app/gradlew .

RUN ./gradlew --no-daemon dependencies

# Копируем исходники и конфиг из app
COPY app/src src
COPY app/config config

RUN ./gradlew --no-daemon shadowJar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=50.0"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar build/libs/app-1.0-SNAPSHOT-all.jar"]
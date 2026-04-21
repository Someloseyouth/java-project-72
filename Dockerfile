FROM eclipse-temurin:21-jdk

WORKDIR /app

# Копируем Gradle wrapper и конфиги
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

# Качаем зависимости (кешируется отдельным слоем)
RUN ./gradlew --no-daemon dependencies

# Копируем исходники и конфиг чекстайла
COPY src src
COPY config config

# Собираем fat-jar через shadow
RUN ./gradlew --no-daemon shadowJar

# Опциональные настройки JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=50.0"

# Документируем порт (Render всё равно перезапишет PORT переменной окружения)
EXPOSE 8080

# Запускаем fat-jar, который содержит hexlet.code.App с Javalin
CMD ["sh", "-c", "java $JAVA_OPTS -jar build/libs/app-1.0-SNAPSHOT-all.jar"]
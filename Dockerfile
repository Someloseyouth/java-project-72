FROM eclipse-temurin:21-jdk

WORKDIR /app

# Копируем Gradle wrapper и конфиги из app
COPY app/gradle gradle
COPY app/build.gradle.kts .
COPY app/settings.gradle.kts .
COPY app/gradlew .

# Качаем зависимости (кешируется отдельным слоем)
RUN ./gradlew --no-daemon dependencies

# Копируем исходники из app
COPY app/src src

# Копируем конфиг checkstyle из корня репозитория
COPY config config

# Собираем fat-jar через shadow
RUN ./gradlew --no-daemon shadowJar

# Опциональные настройки JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=50.0"

# Документируем порт (Railway всё равно прокинет свой PORT)
EXPOSE 8080

# Запускаем fat-jar, который содержит hexlet.code.App с Javalin
CMD ["sh", "-c", "java $JAVA_OPTS -jar build/libs/app-1.0-SNAPSHOT-all.jar"]
# Page Analyzer

Сервис для анализа веб‑страниц (учебный проект Hexlet).  
Приложение позволяет добавлять сайты, выполнять их проверки, сохранять HTTP‑статус и метаданные страницы (`h1`, `title`, `description`), а затем просматривать историю проверок.

### Hexlet tests and linter status:
[![Actions Status](https://github.com/Someloseyouth/java-project-72/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/Someloseyouth/java-project-72/actions)
[![Java CI](https://github.com/Someloseyouth/java-project-72/actions/workflows/build.yml/badge.svg)](https://github.com/Someloseyouth/java-project-72/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Someloseyouth_java-project-72&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Someloseyouth_java-project-72)
![Test Coverage](.github/badges/jacoco.svg)

## Deploy

▶️ [Открыть задеплоенное приложение на Railway](https://java-project-72-production-f7f9.up.railway.app/)

На продакшене приложение работает с базой данных PostgreSQL (Railway), в локальном режиме по умолчанию используется встроенная H2.  
На главной странице доступна форма для добавления нового URL, а также список уже добавленных сайтов с датой и результатом последней проверки.

## Функциональность

- Добавление URL для последующего анализа.
- Валидация введённого адреса и обработка невалидных URL (ответ 422 с сообщением об ошибке).
- Проверка сайта: HTTP‑запрос к сохранённому адресу и сохранение результата.
- Сохранение и отображение:
    - кода ответа (HTTP‑статус);
    - заголовка страницы `<title>`;
    - первого заголовка `<h1>`;
    - описания из `<meta name="description">`.
- Страница списка URL с отображением даты и статуса последней проверки (оптимизирована от N+1 запросов).
- Страница конкретного URL с историей всех проверок.

## Технологии

- Java 21
- Gradle 8.x
- Javalin 7.2.0 (Jetty)
- H2 (in‑memory) для разработки и тестов
- PostgreSQL на продакшене (Railway)
- OkHttp MockWebServer для мокирования HTTP‑ответов в тестах
- JUnit 5, AssertJ для модульных и интеграционных тестов
- Jacoco для измерения покрытия тестами
- SonarCloud для анализа качества кода

## Локальный запуск

### Требования

- Java 21
- Gradle (можно использовать gradle wrapper из проекта)
- Git

### Шаги

1. Клонировать репозиторий:

   ```bash
   git clone https://github.com/Someloseyouth/java-project-72.git
   cd java-project-72/app
   ```

2. Запустить приложение:

   ```bash
   ./gradlew run
   ```

3. Открыть в браузере:

   ```text
   http://localhost:8080
   ```

   На главной странице есть форма с полем `url`, куда можно ввести адрес сайта для анализа.

> Примечание: в локальном режиме приложение работает с базой данных H2 в памяти, для продакшена на Railway используется PostgreSQL с конфигурацией через переменные окружения.

## Тесты

Для запуска всех тестов:

```bash
./gradlew test
```

Тесты проверяют:

- отображение главной страницы и формы добавления URL;
- создание нового URL и повторное добавление уже существующего;
- обработку некорректного URL (ответ 422 и сообщение «Некорректный URL»);
- отображение списка URL и страницы конкретного URL;
- создание успешной проверки (код ответа, `h1`, `title`, `description`);
- обработку ошибки при проверке (например, ответ 500 от проверяемого сайта и вывод сообщения об ошибке).

## Структура проекта (кратко)

- `src/main/java/hexlet/code/App` — точка входа приложения и конфигурация Javalin.
- `src/main/java/hexlet/code/controller` — контроллер (обработчики HTTP‑маршрутов).
- `src/main/java/hexlet/code/repository` — работа с БД (H2/PostgreSQL, JDBC).
- `src/main/java/hexlet/code/model` — модели данных (`Url`, `UrlCheck`).
- `src/test/java/hexlet/code/AppTest.java` — интеграционные тесты веб‑интерфейса и логики проверок.
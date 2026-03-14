#
#FROM openjdk:21-jdk-slim AS build
#
## Устанавливаем рабочую директорию
#WORKDIR /dispatcher
#
## Копируем исходный код
#COPY . .
#
## Собираем проект
#RUN ./gradlew clean bootJar

# Финальный этап
FROM eclipse-temurin:21-jdk-jammy

# Устанавливаем рабочую директорию
WORKDIR /dispatcher

# Копируем JAR-файл из этапа сборки
COPY build/libs/Dispatcher-0.0.1-SNAPSHOT.jar dispatcher.jar

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "dispatcher.jar"]
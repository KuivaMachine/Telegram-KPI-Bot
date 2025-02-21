## Этап сборки
#FROM openjdk:21-jdk-slim AS build
#
## Устанавливаем рабочую директорию
#WORKDIR /googlesheetservice
## Копируем исходный код
#COPY . .
#
#RUN ./gradlew clean bootJar

# Финальный этап
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app
# Копируем JAR-файл из этапа сборки
COPY build/libs/GoogleSheetService-0.0.1-SNAPSHOT.jar googlesheetservice.jar
# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "googlesheetservice.jar"]
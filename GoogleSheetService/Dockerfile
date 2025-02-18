# Этап сборки
FROM openjdk:21-jdk-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /googlesheetservice

# Копируем исходный код
COPY . .
#COPY ../.env /shared/
# Собираем проект
RUN ./gradlew clean bootJar

# Финальный этап
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл из этапа сборки
COPY --from=build /googlesheetservice/build/libs/GoogleSheetService-0.0.1-SNAPSHOT.jar googlesheetservice.jar
#COPY --from=build /shared/.env /googlesheetservice/.env
# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "googlesheetservice.jar"]
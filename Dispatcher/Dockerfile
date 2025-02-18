# Этап сборки
FROM openjdk:21-jdk-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /dispatcher

# Копируем исходный код
COPY . .

# Собираем проект
RUN ./gradlew clean bootJar

# Финальный этап
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл из этапа сборки
COPY --from=build /dispatcher/build/libs/Dispatcher-0.0.1-SNAPSHOT.jar dispatcher.jar

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "dispatcher.jar"]
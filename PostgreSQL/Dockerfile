
FROM openjdk:21-jdk-slim as build

ENV HOME=/dispatcher
WORKDIR $HOME


COPY . .
RUN ./gradlew clean build
# Создаем новый этап для финального образа
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR $HOME

# Копируем собранный JAR-файл из предыдущего этапа
COPY --from=build $HOME/build/libs/*.jar dispatcher.jar


# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "dispatcher.jar"]
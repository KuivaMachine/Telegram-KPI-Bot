<deTelegram-KPI-Bot 🤖

📊 Telegram-KPI-Bot — это телеграм-бот для сбора статистики работников компании "MustHaveCase". Он помогает собирать данные от печатников 🖨️ и упаковщиков 📦, сохраняет их в базу данных PostgreSQL 🐘, а затем автоматически заполняет таблицу Google Sheets 📈.
🚀 Основные функции
- Добавление статистики ➕: Работники могут отправлять свои данные через бота.
- Показ последней записи 👀: Бот может показать последнюю внесенную запись.
- Удаление записи 🗑️: Возможность удалить ошибочно внесенную запись.
- Сохранение в БД 💾: Все данные сохраняются в PostgreSQL.
- Генерация таблицы Google Sheets 📊: Данные автоматически переносятся в таблицу Google Sheets.

🛠️ Технологии и инструменты
Языки и фреймворки
- Java ☕
- Spring 🌱 (Spring Boot, Spring Kafka, Spring Data JPA)

Базы данных

- PostgreSQL 🐘

Брокер сообщений
- Kafka 🚀
- 
Интеграции
- Google Sheets API 📈
- Telegram Bot API 🤖

Инфраструктура
- Docker 🐳 (каждый модуль имеет свой Dockerfile для развертывания)

- Микросервисная архитектура 🧩

🧩 Модули приложения
- 1. Dispatcher 📨

    Отвечает за взаимодействие с Telegram Bot API.

    Принимает данные от пользователей и отправляет их в Kafka.

- 2. GoogleSheetService 📊

    Получает данные из Kafka.
    Сохраняет данные в PostgreSQL.
    Обновляет таблицу Google Sheets на основе полученных данных.

🐳 Развертывание с Docker

Каждый модуль приложения имеет свой Dockerfile, что позволяет легко развернуть приложение на сервере.

    Соберите Docker-образы для каждого модуля:
    bash
    Copy

    docker build -t dispatcher -f Dispatcher/Dockerfile .
    docker build -t google-sheet-service -f GoogleSheetService/Dockerfile .

    Запустите контейнеры:
    bash
    Copy

    docker run -d --name dispatcher dispatcher
    docker run -d --name google-sheet-service google-sheet-service

    Убедитесь, что Kafka и PostgreSQL также запущены и настроены.

🚀 Как начать

    Клонируйте репозиторий:
    bash
    Copy

    git clone https://github.com/ваш-username/Telegram-KPI-Bot.git

    Настройте переменные окружения:

        TELEGRAM_BOT_TOKEN: Токен вашего Telegram-бота.

        GOOGLE_SHEETS_CREDENTIALS: JSON-файл с учетными данными для Google Sheets API.

        DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD: Данные для подключения к PostgreSQL.

        KAFKA_BROKER: Адрес брокера Kafka.

    Запустите приложение:

        Соберите и запустите Docker-контейнеры (см. раздел Развертывание с Docker).

📄 Лицензия

Этот проект распространяется под лицензией MIT. Подробности см. в файле LICENSE.
📬 Контакты

Если у вас есть вопросы или предложения, свяжитесь со мной:

    Telegram: @ваш-username

    Email: ваш-email@example.com

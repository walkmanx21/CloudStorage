# Приложение CloudStorage (облачное хранилище)

Многопользовательское файловое облако. Пользователи сервиса могут использовать его для загрузки и хранения файлов.

## Стек
- Java 17
- Spring Boot 3.5.5
- Gradle

## Требования
- JDK 17+
- Gradle Wrapper
- Наличие Docker

## Инструкция по локальному запуску проекта
1. Необходимо обеспечить наличие .env файла с указанием переменных. Если планируется подключение к БД PostgreSQL и Redis из среды разработки, требуется прописать переменные окружения из .env файла в операционную систему (после внесения, перезагрузить систему).
2. Запуск docker-контейнеров (PostgreSQL, Adminer, Redis, MinIO): docker-compose up -d
3. Запуск приложения. 

После запуска приложение доступно по адресу: http://localhost:8080/

Swagger UI доступен по адресу: http://localhost:8080/swagger-ui/index.html

## Конфигурация (содержание .env файла)
- Для работы БД:
  - POSTGRES_USER=your_user
  - POSTGRES_PASSWORD=your_password
- Для работы Redis:
  - REDIS_PASSWORD=your_password
- Для работы MinIO:
  - MINIO_ROOT_USER=your_user
  - MINIO_ROOT_PASSWORD=your_password

## Adminer
Для просмотра и администрирования БД реализован веб-интерфейс Adminer, доступный по адресу: http://localhost:8081/

Параметры входа в Adminer:
- Движок: PostgreSQL
- Сервер: postgres
- Имя пользователя: в соответствии с POSTGRES_USER (из .env)
- Пароль: в соответствии с POSTGRES_PASSWORD (из .env)
- База данных: cloud_storage_db

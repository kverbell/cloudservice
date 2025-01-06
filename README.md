# CloudService

## Описание

Облачный сервис, предназначенный для хранения файлов. 

Приложение использует Spring Boot и базу данных PostgreSQL для хранения информации о файлах и пользователях.

## Технические характеристики

- **Spring Boot** версии 3.3.3
- **Java** версии 17
- **PostgreSQL**
- Используются следующие зависимости:
    - Spring Boot Actuator
    - Spring Boot Data JPA
    - Spring Boot Security
    - Lombok для упрощения кода
    - Hibernate ORM
    - Testcontainers для тестирования
    - JUnit для тестов

## Запуск клиента

Для запуска клиента следуйте [инструкции](https://github.com/netology-code/jd-homeworks/blob/master/diploma/cloudservice.md#:~:text=%D0%9E%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5%20%D0%B8%20%D0%B7%D0%B0%D0%BF%D1%83%D1%81%D0%BA%20FRONT).

## Запуск сервера

### Требования:
1. Установите Java Runtime Environment (JRE) версии 17 или выше.
2. Скачайте или клонируйте проект на ваше устройство.

### Шаги для запуска сервера:
1. Откройте PowerShell или терминал.
2. Затем нужно открыть JAR-файл, последовательно введя в PowerShell или терминале команды:
```md
cd "путь к приложению\cloudservice"

java -jar cloudservice-0.0.1-SNAPSHOT.jar
```
3.	Для завершения работы нужно закрыть PowerShell или терминал.

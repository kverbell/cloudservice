# Образ OpenJDK 17
FROM openjdk:17

# Упаковка JAR-файла в контейнер
COPY cloudservice-0.0.1-SNAPSHOT.jar /app/cloudservice.jar

# Определение рабочей директории
WORKDIR /app

# Команда для запуска Java-приложения
CMD ["java", "-jar", "/app/cloudservice.jar"]
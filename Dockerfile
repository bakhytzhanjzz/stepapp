# -------- Stage 1: Build --------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Сначала копируем только pom.xml для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Теперь копируем исходники и собираем jar
COPY src ./src
RUN mvn clean package -DskipTests

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jdk AS runtime

WORKDIR /app

# Копируем jar из builder
COPY --from=builder /app/target/*.jar app.jar

# Экспонируем порт (Render сам подхватит)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]

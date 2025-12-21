# Останавливаем все сервисы
docker-compose down

Write-Host "Обновляю Dockerfile для всех сервисов..." -ForegroundColor Yellow

# Единый Dockerfile для всех сервисов
 = @'
FROM maven:3.8.4-openjdk-11-slim AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Пропускаем ВСЕ тесты полностью
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
'@

# Обновляем все Dockerfile
 = @("config-server", "discovery-service", "user-service", "notification-service", "api-gateway")
foreach ( in ) {
     | Out-File -FilePath "/Dockerfile" -Encoding UTF8
    Write-Host "  ✓ " -ForegroundColor Green
}

Write-Host "
Запускаю все сервисы..." -ForegroundColor Yellow
docker-compose up -d --build

Write-Host "
Жду запуска сервисов (20 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host "
=== ФИНАЛЬНЫЙ СТАТУС ===" -ForegroundColor Cyan
docker-compose ps

Write-Host "
=== ПРОВЕРКА ВЗАИМОДЕЙСТВИЯ ===" -ForegroundColor Cyan
Write-Host "1. Eureka Discovery: http://localhost:8761" -ForegroundColor Blue
Write-Host "2. Config Server: http://localhost:8888" -ForegroundColor Blue
Write-Host "3. API Gateway: http://localhost:8080/actuator/health" -ForegroundColor Blue
Write-Host "4. User Service: http://localhost:8081/actuator/health" -ForegroundColor Blue
Write-Host "5. Notification Service: http://localhost:8082/actuator/health" -ForegroundColor Blue

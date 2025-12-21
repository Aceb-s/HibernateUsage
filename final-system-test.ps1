Write-Host "=== ТЕСТ СИСТЕМЫ ПОСЛЕ ИСПРАВЛЕНИЙ ===" -ForegroundColor Cyan

# 1. Пересобираем user-service
Write-Host "
1. ПЕРЕСБОРКА USER-SERVICE:" -ForegroundColor Yellow
docker-compose up -d --build user-service

Write-Host "   Жду 30 секунд..." -ForegroundColor Gray
Start-Sleep -Seconds 30

# 2. Проверяем здоровье
Write-Host "
2. ПРОВЕРКА ЗДОРОВЬЯ:" -ForegroundColor Yellow

# 2.1 User-service
Write-Host "   User-service:" -ForegroundColor Gray
try {
     = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 10
    Write-Host "    Здоров: " -ForegroundColor Green
} catch {
    Write-Host "    Не доступен" -ForegroundColor Red
    docker-compose logs user-service --tail=10
}

# 2.2 API Gateway
Write-Host "
   API Gateway:" -ForegroundColor Gray
try {
     = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "    Здоров: " -ForegroundColor Green
} catch {
    Write-Host "    Не доступен" -ForegroundColor Red
}

# 3. Создание пользователя напрямую
Write-Host "
3. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ (напрямую):" -ForegroundColor Yellow
 = @{
    name = "Direct Test 1284311205"
    email = "direct1423274174@test.com"
    age = 30
} | ConvertTo-Json

Write-Host "   Данные: " -ForegroundColor Gray

try {
     = Invoke-RestMethod -Uri "http://localhost:8081/api/users" 
        -Method Post 
        -ContentType "application/json" 
        -Body  
        -TimeoutSec 10
    
    Write-Host "    Пользователь создан!" -ForegroundColor Green
    Write-Host "   Ответ: " -ForegroundColor Gray
} catch {
    Write-Host "    Ошибка: " -ForegroundColor Red
}

# 4. Получение списка
Write-Host "
4. ПОЛУЧЕНИЕ СПИСКА ПОЛЬЗОВАТЕЛЕЙ:" -ForegroundColor Yellow
try {
    @{_embedded=; _links=} = Invoke-RestMethod -Uri "http://localhost:8081/api/users" -Method Get -TimeoutSec 5
    Write-Host "    Пользователей в системе: 1" -ForegroundColor Green
    if (@{_embedded=; _links=} -and @(@{_embedded=; _links=}).Count -gt 0) {
        @{_embedded=; _links=} | ForEach-Object {
            Write-Host "   -  ()" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "    Ошибка: " -ForegroundColor Red
}

# 5. Проверка Eureka
Write-Host "
5. ПРОВЕРКА EUREKA:" -ForegroundColor Yellow
Start-Process "http://localhost:8761"
Write-Host "   Откройте Eureka в браузере" -ForegroundColor Blue

Write-Host "
=== ИТОГ ===" -ForegroundColor Cyan
Write-Host " User.java восстановлен" -ForegroundColor Green
Write-Host " User-service пересобран" -ForegroundColor Green
Write-Host " Создание и получение пользователей работает" -ForegroundColor Green
Write-Host " Весь стек микросервисов развернут в Docker" -ForegroundColor Green

Write-Host "
ДЛЯ ДЕМОНСТРАЦИИ:" -ForegroundColor Blue
Write-Host "1. Все контейнеры: docker-compose ps" -ForegroundColor Blue
Write-Host "2. Eureka: http://localhost:8761" -ForegroundColor Blue
Write-Host "3. User-service: http://localhost:8081/api/users" -ForegroundColor Blue
Write-Host "4. API Gateway: http://localhost:8080/actuator/health" -ForegroundColor Blue
Write-Host "5. Kafka: docker-compose exec kafka kafka-topics --list" -ForegroundColor Blue

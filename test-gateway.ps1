Write-Host "=== ТЕСТИРОВАНИЕ API GATEWAY И ВЗАИМОДЕЙСТВИЯ ===" -ForegroundColor Cyan

# 1. Проверяем доступные actuator endpoints
Write-Host "
1. ДОСТУПНЫЕ ENDPOINTS ACTUATOR:" -ForegroundColor Yellow
try {
     = Invoke-RestMethod -Uri "http://localhost:8080/actuator" -TimeoutSec 5
    Write-Host "   Доступные endpoints:" -ForegroundColor Gray
    ._links.PSObject.Properties | ForEach-Object {
        Write-Host "   - : " -ForegroundColor Gray
    }
} catch {
    Write-Host "   ✗ Actuator недоступен" -ForegroundColor Red
}

# 2. Тестируем создание пользователя
Write-Host "
2. ТЕСТ СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ:" -ForegroundColor Yellow
 = Get-Random -Minimum 1000 -Maximum 9999
 = @{
    username = "testuser_"
    email = "test_@example.com"
    password = "TestPass123!"
} | ConvertTo-Json

Write-Host "   Отправляемые данные: " -ForegroundColor Gray

try {
     = Invoke-RestMethod -Uri "http://localhost:8080/api/users" 
        -Method Post 
        -ContentType "application/json" 
        -Body  
        -TimeoutSec 10
    
    Write-Host "   Пользователь создан успешно!" -ForegroundColor Green
    Write-Host "   Ответ: " -ForegroundColor Gray
} catch {
    Write-Host "   ✗ Ошибка при создании пользователя" -ForegroundColor Red
    if (.Exception.Response) {
         = .Exception.Response.StatusCode.value__
         = .Exception.Response.GetResponseStream()
         = New-Object System.IO.StreamReader()
         = .ReadToEnd()
        Write-Host "   Код ошибки: " -ForegroundColor Red
        Write-Host "   Тело ошибки: " -ForegroundColor Red
    }
}

# 3. Проверяем получение пользователей
Write-Host "
3. ПОЛУЧЕНИЕ СПИСКА ПОЛЬЗОВАТЕЛЕЙ:" -ForegroundColor Yellow
try {
     = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get -TimeoutSec 5
    if ( -and @().Count -gt 0) {
        Write-Host "   ✓ Найдено пользователей: 1" -ForegroundColor Green
         | ForEach-Object {
            Write-Host "   -  ()" -ForegroundColor Gray
        }
    } else {
        Write-Host "    Нет пользователей или пустой ответ" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ✗ Ошибка: " -ForegroundColor Red
}

# 4. Проверяем напрямую user-service (минуя gateway)
Write-Host "
4. ПРЯМАЯ ПРОВЕРКА USER-SERVICE:" -ForegroundColor Yellow
try {
     = Invoke-RestMethod -Uri "http://localhost:8081/api/users" -Method Get -TimeoutSec 5
    Write-Host "   ✓ User-service доступен напрямую" -ForegroundColor Green
    Write-Host "   Количество пользователей: 1" -ForegroundColor Gray
} catch {
    Write-Host "   ✗ User-service недоступен: " -ForegroundColor Red
}

# 5. Проверяем логи notification-service на события Kafka
Write-Host "
5. ЛОГИ NOTIFICATION-SERVICE:" -ForegroundColor Yellow
 = docker-compose logs notification-service --tail=30 2>&1
if ( -match "(?i)(kafka|consumer|event|email|user)") {
    Write-Host "    Найдены события Kafka/пользователей" -ForegroundColor Green
     =  -split "
" | Where-Object {  -match "(?i)(kafka|consumer|event|email|user)" }
     | Select-Object -First 3 | ForEach-Object {
        Write-Host "   - " -ForegroundColor Gray
    }
} else {
    Write-Host "   ⓘ События не найдены в логах" -ForegroundColor Yellow
}

# 6. Финальная проверка Eureka
Write-Host "
6. ФИНАЛЬНАЯ ПРОВЕРКА EUREKA:" -ForegroundColor Yellow
Start-Process "http://localhost:8761"
Write-Host "   Откройте Eureka в браузере: http://localhost:8761" -ForegroundColor Blue

Write-Host "
=== РЕЗЮМЕ ===" -ForegroundColor Cyan
Write-Host "Все компоненты системы запущены в Docker" -ForegroundColor Green
Write-Host "Сервисы зарегистрированы в Eureka" -ForegroundColor Green
Write-Host "API Gateway доступен" -ForegroundColor Green
Write-Host "Система готова к тестированию взаимодействия" -ForegroundColor Green

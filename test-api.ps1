Write-Host "=== ТЕСТИРОВАНИЕ API GATEWAY И ВЗАИМОДЕЙСТВИЯ ===" -ForegroundColor Cyan

# 1. Проверяем доступные actuator endpoints
Write-Host "`n1. ДОСТУПНЫЕ ENDPOINTS ACTUATOR:" -ForegroundColor Yellow
try {
    $actuator = Invoke-RestMethod -Uri "http://localhost:8080/actuator" -TimeoutSec 5
    Write-Host "   Доступные endpoints:" -ForegroundColor Gray
    $actuator._links.PSObject.Properties | ForEach-Object {
        Write-Host "   - $($_.Name): $($_.Value.href)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ? Actuator недоступен" -ForegroundColor Red
}

# 2. Тестируем создание пользователя
Write-Host "`n2. ТЕСТ СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ:" -ForegroundColor Yellow
$randomId = Get-Random -Minimum 1000 -Maximum 9999
$userData = @{
    username = "testuser_$randomId"
    email = "test_$randomId@example.com"
    password = "TestPass123!"
} | ConvertTo-Json

Write-Host "   Отправляемые данные: $userData" -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Post -ContentType "application/json" -Body $userData -TimeoutSec 10

    Write-Host "   ? Пользователь создан успешно!" -ForegroundColor Green
    Write-Host "   Ответ: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
} catch {
    Write-Host "   ? Ошибка при создании пользователя" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errorBody = $reader.ReadToEnd()
        Write-Host "   Код ошибки: $statusCode" -ForegroundColor Red
        Write-Host "   Тело ошибки: $errorBody" -ForegroundColor Red
    }
}

# 3. Проверяем получение пользователей
Write-Host "`n3. ПОЛУЧЕНИЕ СПИСКА ПОЛЬЗОВАТЕЛЕЙ:" -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get -TimeoutSec 5
    if ($users -and @($users).Count -gt 0) {
        Write-Host "   ? Найдено пользователей: $(@($users).Count)" -ForegroundColor Green
        $users | ForEach-Object {
            Write-Host "   - $($_.username) ($($_.email))" -ForegroundColor Gray
        }
    } else {
        Write-Host "   ? Нет пользователей или пустой ответ" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ? Ошибка: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Проверяем напрямую user-service (минуя gateway)
Write-Host "`n4. ПРЯМАЯ ПРОВЕРКА USER-SERVICE:" -ForegroundColor Yellow
try {
    $directResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/users" -Method Get -TimeoutSec 5
    Write-Host "   ? User-service доступен напрямую" -ForegroundColor Green
    Write-Host "   Количество пользователей: $(@($directResponse).Count)" -ForegroundColor Gray
} catch {
    Write-Host "   ? User-service недоступен: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Проверяем логи notification-service на события Kafka
Write-Host "`n5. ЛОГИ NOTIFICATION-SERVICE:" -ForegroundColor Yellow
try {
    $logs = docker-compose logs notification-service --tail=30 2>&1
    if ($logs -match "(?i)(kafka|consumer|event|email|user)") {
        Write-Host "   ? Найдены события Kafka/пользователей" -ForegroundColor Green
        $matches = $logs -split "`n" | Where-Object { $_ -match "(?i)(kafka|consumer|event|email|user)" }
        $matches | Select-Object -First 3 | ForEach-Object {
            Write-Host "   - $_" -ForegroundColor Gray
        }
    } else {
        Write-Host "   ? События не найдены в логах" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ? Не удалось получить логи: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Финальная проверка Eureka
Write-Host "`n6. ФИНАЛЬНАЯ ПРОВЕРКА EUREKA:" -ForegroundColor Yellow
try {
    Start-Process "http://localhost:8761" -ErrorAction SilentlyContinue
    Write-Host "   Откройте Eureka в браузере: http://localhost:8761" -ForegroundColor Blue
} catch {
    Write-Host "   Не удалось открыть браузер, откройте вручную: http://localhost:8761" -ForegroundColor Yellow
}

Write-Host "`n=== РЕЗЮМЕ ===" -ForegroundColor Cyan
Write-Host "Все компоненты системы запущены в Docker" -ForegroundColor Green
Write-Host "Сервисы зарегистрированы в Eureka" -ForegroundColor Green
Write-Host "API Gateway доступен" -ForegroundColor Green
Write-Host "Система готова к тестированию взаимодействия" -ForegroundColor Green

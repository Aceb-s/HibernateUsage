Write-Host "=== ТЕСТ GATEWAY С ОБНОВЛЕННОЙ КОНФИГУРАЦИЕЙ ===" -ForegroundColor Cyan

# 1. Проверяем здоровье
Write-Host "
1. HEALTH CHECK:" -ForegroundColor Yellow
try {
    @{status=UP; components=} = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "    Gateway здоров: UP" -ForegroundColor Green
} catch {
    Write-Host "    Gateway недоступен" -ForegroundColor Red
}

# 2. Тест через Gateway
Write-Host "
2. TEST API THROUGH GATEWAY:" -ForegroundColor Yellow

# 2.1 GET запрос
Write-Host "   GET /api/users:" -ForegroundColor Gray
try {
    @{_embedded=; _links=} = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get -TimeoutSec 10
    Write-Host "    Успех! Пользователей: 1" -ForegroundColor Green
    if (@{_embedded=; _links=} -and @(@{_embedded=; _links=}).Count -gt 0) {
        @{_embedded=; _links=} | Select-Object -First 2 | ForEach-Object {
            Write-Host "   - : " -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "    Ошибка GET: " -ForegroundColor Red
}

# 2.2 POST запрос
Write-Host "
   POST /api/users:" -ForegroundColor Gray
 = @{
    name = "Gateway Test 986203113"
    email = "gateway675210262@test.com"
    age = 32
} | ConvertTo-Json

try {
     = Invoke-RestMethod -Uri "http://localhost:8080/api/users" 
        -Method Post 
        -ContentType "application/json" 
        -Body  
        -TimeoutSec 10
    
    Write-Host "    Пользователь создан через Gateway!" -ForegroundColor Green
    Write-Host "   Ответ: " -ForegroundColor Gray
} catch {
    Write-Host "    Ошибка POST: " -ForegroundColor Red
    
    # Проверяем детали ошибки
    if (.Exception.Response) {
        System.Net.SyncMemoryStream = .Exception.Response.GetResponseStream()
        System.IO.StreamReader = New-Object System.IO.StreamReader(System.Net.SyncMemoryStream)
        {"error":"Internal server error"} = System.IO.StreamReader.ReadToEnd()
        Write-Host "   Детали: {"error":"Internal server error"}" -ForegroundColor Red
    }
}

# 3. Проверяем Eureka
Write-Host "
3. EUREKA REGISTRATION:" -ForegroundColor Yellow
try {
     = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -TimeoutSec 5
    if ( -match "API-GATEWAY") {
        Write-Host "    API-GATEWAY зарегистрирован в Eureka" -ForegroundColor Green
    }
    if ( -match "USER-SERVICE") {
        Write-Host "    USER-SERVICE зарегистрирован в Eureka" -ForegroundColor Green
    }
} catch {
    Write-Host "    Не удалось проверить Eureka" -ForegroundColor Yellow
}

Write-Host "
=== ДИАГНОСТИКА ЕСЛИ НЕ РАБОТАЕТ ===" -ForegroundColor Cyan
Write-Host "Если Gateway не работает, проверьте:" -ForegroundColor Gray
Write-Host "1. Логи Gateway: docker-compose logs api-gateway --tail=30" -ForegroundColor Blue
Write-Host "2. Прямой доступ: http://localhost:8081/api/users" -ForegroundColor Blue
Write-Host "3. Eureka: http://localhost:8761" -ForegroundColor Blue

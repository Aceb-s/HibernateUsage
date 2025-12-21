Write-Host "=== ПОЛНОЕ ИСПРАВЛЕНИЕ KAFKA/ZOOKEEPER ===" -ForegroundColor Cyan

# 1. Остановить всё
Write-Host "1. Останавливаю все контейнеры..." -ForegroundColor Yellow
docker-compose down 2>$null
docker rm -f kafka zookeeper 2>$null

# 2. Очистить данные
Write-Host "2. Очищаю данные Zookeeper..." -ForegroundColor Yellow
docker volume prune -f 2>$null

# 3. Запустить Zookeeper
Write-Host "3. Запускаю Zookeeper..." -ForegroundColor Yellow
docker run -d `
  --name zookeeper `
  -p 2181:2181 `
  -e ZOOKEEPER_CLIENT_PORT=2181 `
  confluentinc/cp-zookeeper:7.4.0

Write-Host "   Жду 10 секунд..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# 4. Запустить Kafka
Write-Host "4. Запускаю Kafka с чистым состоянием..." -ForegroundColor Yellow
docker run -d `
  --name kafka `
  -p 9092:9092 `
  -p 29092:29092 `
  -e KAFKA_BROKER_ID=1 `
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092 `
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT `
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT_INTERNAL `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 `
  --link zookeeper `
  confluentinc/cp-kafka:7.4.0

Write-Host "5. Жду запуска Kafka (20 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# 5. Проверить Kafka
Write-Host "`n6. Проверяю Kafka..." -ForegroundColor Cyan
docker logs kafka --tail=5

try {
    docker exec kafka kafka-topics --list --bootstrap-server localhost:9092 2>&1
    Write-Host "   ? Kafka запущена успешно!" -ForegroundColor Green
} catch {
    Write-Host "   ? Ошибка проверки Kafka: $_" -ForegroundColor Red
}

# 6. Запустить остальные сервисы
Write-Host "`n7. Запускаю остальные сервисы..." -ForegroundColor Yellow
docker-compose up -d postgres discovery-service

Start-Sleep -Seconds 10

docker-compose up -d user-service notification-service api-gateway

# 7. Финальная проверка
Write-Host "`n8. Финальная проверка (ждем 15 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`n=== РЕЗУЛЬТАТ ===" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

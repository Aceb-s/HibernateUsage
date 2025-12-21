Write-Host "Создаю конфигурационные файлы для Docker..." -ForegroundColor Yellow

# 1. Создаем для notification-service
 = @'
server:
  port: 8082

spring:
  application:
    name: notification-service
  
  kafka:
    bootstrap-servers: kafka:9092
    consumer:
      group-id: notification-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
        spring.json.value.default.type: org.example.notification.dto.EmailRequest

  mail:
    host: smtp.gmail.com
    port: 587
    username: 
    password: 
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
    enabled: true
  instance:
    prefer-ip-address: true
    hostname: notification-service

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
'@

# Создаем директорию если нет
New-Item -ItemType Directory -Path "notification-service/src/main/resources" -Force
 | Out-File -FilePath "notification-service/src/main/resources/application-docker.yml" -Encoding UTF8
Write-Host "✓ Создан application-docker.yml для notification-service" -ForegroundColor Green

# 2. Создаем для api-gateway
 = @'
server:
  port: 8080

spring:
  application:
    name: api-gateway
  
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/(?<segment>.*), /$\{segment}
        
        - id: notification-service
          uri: lb://NOTIFICATION-SERVICE  
          predicates:
            - Path=/api/notifications/**
          filters:
            - RewritePath=/api/(?<segment>.*), /$\{segment}

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
    enabled: true
  instance:
    prefer-ip-address: true
    hostname: api-gateway

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
'@

New-Item -ItemType Directory -Path "api-gateway/src/main/resources" -Force
 | Out-File -FilePath "api-gateway/src/main/resources/application-docker.yml" -Encoding UTF8
Write-Host "✓ Создан application-docker.yml для api-gateway" -ForegroundColor Green

# 3. Создаем для discovery-service (если еще нет)
 = @'
server:
  port: 8761

spring:
  application:
    name: discovery-service

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
'@

New-Item -ItemType Directory -Path "discovery-service/src/main/resources" -Force
 | Out-File -FilePath "discovery-service/src/main/resources/application-docker.yml" -Encoding UTF8
Write-Host "✓ Создан application-docker.yml для discovery-service" -ForegroundColor Green

# 4. Также нужен для config-server
 = @'
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: 
          clone-on-start: true
          default-label: main

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
'@

New-Item -ItemType Directory -Path "config-server/src/main/resources" -Force
 | Out-File -FilePath "config-server/src/main/resources/application-docker.yml" -Encoding UTF8
Write-Host "✓ Создан application-docker.yml для config-server" -ForegroundColor Green

Write-Host "
Пересобираем все сервисы..." -ForegroundColor Yellow
docker-compose down
docker-compose up -d --build

Write-Host "
Ждем 40 секунд для полного запуска и регистрации..." -ForegroundColor Yellow
Start-Sleep -Seconds 40

Write-Host "
=== ПРОВЕРКА РЕГИСТРАЦИИ В EUREKA ===" -ForegroundColor Cyan
try {
     = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -TimeoutSec 10
    Write-Host "Eureka API ответ:" -ForegroundColor Green
    .Content
} catch {
    Write-Host "Ошибка: " -ForegroundColor Red
}

Write-Host "
=== СТАТУС КОНТЕЙНЕРОВ ===" -ForegroundColor Cyan
docker-compose ps

Write-Host "
Откройте Eureka UI: http://localhost:8761" -ForegroundColor Blue

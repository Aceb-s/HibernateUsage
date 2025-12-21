package org.example.apigateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service с Circuit Breaker
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/user-service"))
                                .rewritePath("/api/users/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Service-Name", "user-service"))
                        .uri("lb://USER-SERVICE"))

                // Swagger для User Service
                .route("user-service-swagger", r -> r
                        .path("/user-service/swagger-ui.html", "/user-service/webjars/**",
                                "/user-service/swagger-resources/**", "/user-service/v2/api-docs")
                        .filters(f -> f.rewritePath("/user-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://USER-SERVICE"))

                // Notification Service (если есть)
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCB")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                                .rewritePath("/api/notifications/(?<segment>.*)", "/${segment}"))
                        .uri("lb://NOTIFICATION-SERVICE"))

                // Discovery Service UI
                .route("discovery-service", r -> r
                        .path("/discovery/**")
                        .filters(f -> f
                                .rewritePath("/discovery/(?<segment>.*)", "/${segment}")
                                .setPath("/"))
                        .uri("http://localhost:8761"))

                .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .failureRateThreshold(50.0f)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(5)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                .build());
    }
}

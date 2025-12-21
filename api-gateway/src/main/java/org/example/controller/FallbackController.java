package org.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "SERVICE_UNAVAILABLE",
                        "message", "User Service is temporarily unavailable. Please try again later.",
                        "timestamp", System.currentTimeMillis()
                )));
    }

    @GetMapping("/fallback/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "SERVICE_UNAVAILABLE",
                        "message", "Notification Service is temporarily unavailable.",
                        "timestamp", System.currentTimeMillis()
                )));
    }
}

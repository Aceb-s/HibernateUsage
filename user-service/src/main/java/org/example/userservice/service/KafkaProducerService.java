package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserEvent(String action, String email, String name) {
        try {
            String message = String.format("User %s: %s (%s)", action, name, email);
            kafkaTemplate.send("user-events", message);
            log.info("Sent Kafka message: {}", message);
        } catch (Exception e) {
            log.error("Error sending Kafka message: {}", e.getMessage());
        }
    }
}

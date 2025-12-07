package org.example.userservice.service;

import org.example.userservice.dto.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${kafka.topics.user-events}")
    private String userEventsTopic;

    public void sendUserEvent(String operation, String email, String username) {
        try {
            UserEvent event = new UserEvent(operation, email, username);
            kafkaTemplate.send(userEventsTopic, event);
            log.info("Sent user event to Kafka: {}", event);
        } catch (Exception e) {
            log.error("Failed to send user event to Kafka: {}/{}/{}", operation, email, username, e);
        }
    }
}

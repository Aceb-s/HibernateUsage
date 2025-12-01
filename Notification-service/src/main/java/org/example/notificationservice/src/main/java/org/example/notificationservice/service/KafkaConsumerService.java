package org.example.notificationservice.service;

import org.example.notificationservice.dto.UserEventMessage;
import org.example.notificationservice.dto.EmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final EmailService emailService;

    public KafkaConsumerService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${kafka.topics.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvent(UserEventMessage message) {
        logger.info("Received message from Kafka: {}", message);

        try {
            EmailResponse response = emailService.sendUserNotification(
                    message.getEmail(),
                    message.getOperation(),
                    message.getUsername()
            );

            logger.info("Email sent successfully: {}", response);
        } catch (Exception e) {
            logger.error("Failed to process Kafka message: {}", message, e);
        }
    }
}

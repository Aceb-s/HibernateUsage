package org.example.notificationservice.service;

import org.example.notificationservice.dto.EmailRequest;
import org.example.notificationservice.dto.EmailResponse;
import org.example.notificationservice.exception.NotificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public EmailResponse sendEmail(EmailRequest emailRequest) {
        try {
            logger.info("Sending email to: {}", emailRequest.getEmail());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRequest.getEmail());
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getMessage());

            mailSender.send(message);

            logger.info("Email sent successfully to: {}", emailRequest.getEmail());
            return new EmailResponse(true, "Email sent successfully via Mailtrap", emailRequest.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", emailRequest.getEmail(), e.getMessage());
            throw new NotificationException("Failed to send email to " + emailRequest.getEmail(), e);
        }
    }

    public EmailResponse sendUserNotification(String email, String operation, String username) {
        String subject = "";
        String message = "";

        switch (operation.toUpperCase()) {
            case "CREATE":
                subject = "Аккаунт успешно создан";
                message = String.format("Здравствуйте, %s! Ваш аккаунт на сайте был успешно создан.",
                        username != null ? username : "Пользователь");
                break;
            case "DELETE":
                subject = "Аккаунт удален";
                message = String.format("Здравствуйте, %s! Ваш аккаунт был удалён.",
                        username != null ? username : "Пользователь");
                break;
            default:
                throw new NotificationException("Unknown operation: " + operation);
        }

        logger.info("Preparing {} notification for: {} ({})", operation, email, username);
        EmailRequest emailRequest = new EmailRequest(email, subject, message, operation);
        return sendEmail(emailRequest);
    }
}

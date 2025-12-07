package org.example.notificationservice.controller;

import org.example.notificationservice.dto.EmailRequest;
import org.example.notificationservice.dto.EmailResponse;
import org.example.notificationservice.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest emailRequest) {
        EmailResponse response = emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{operation}")
    public ResponseEntity<EmailResponse> sendUserNotification(
            @PathVariable String operation,
            @RequestParam String email,
            @RequestParam(required = false) String username) {

        EmailResponse response = emailService.sendUserNotification(email, operation, username);
        return ResponseEntity.ok(response);
    }
}

package org.example.notification.service;

import org.example.notification.dto.EmailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    public void testSendUserNotification_Create() {
        EmailResponse response = emailService.sendUserNotification(
                "test@example.com", "create", "TestUser");

        assertTrue(response.isSuccess());
        assertEquals("test@example.com", response.getEmail());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendUserNotification_Delete() {
        EmailResponse response = emailService.sendUserNotification(
                "test@example.com", "delete", "TestUser");

        assertTrue(response.isSuccess());
        assertEquals("test@example.com", response.getEmail());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}

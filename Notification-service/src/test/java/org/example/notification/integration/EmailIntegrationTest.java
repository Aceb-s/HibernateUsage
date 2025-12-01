package org.example.notification.integration;

import org.example.notification.dto.EmailRequest;
import org.example.notification.dto.EmailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EmailIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSendEmailAPI() {
        EmailRequest request = new EmailRequest();
        request.setEmail("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        ResponseEntity<EmailResponse> response = restTemplate
                .postForEntity("/api/notifications/email", request, EmailResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    public void testSendUserNotificationAPI() {
        ResponseEntity<EmailResponse> response = restTemplate
                .postForEntity("/api/notifications/user/create?email=test@example.com&username=TestUser",
                        null, EmailResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }
}

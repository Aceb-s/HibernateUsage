package org.example.notificationservice.dto;

public class EmailRequest {
    private String email;
    private String subject;
    private String message;
    private String operationType;

    public EmailRequest() {}

    public EmailRequest(String email, String subject, String message, String operationType) {
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.operationType = operationType;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
}

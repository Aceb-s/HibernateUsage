package org.example.notificationservice.dto;

public class UserEventMessage {
    private String operation;
    private String email;
    private String username;

    public UserEventMessage() {}

    public UserEventMessage(String operation, String email, String username) {
        this.operation = operation;
        this.email = email;
        this.username = username;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "UserEventMessage{" +
                "operation='" + operation + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

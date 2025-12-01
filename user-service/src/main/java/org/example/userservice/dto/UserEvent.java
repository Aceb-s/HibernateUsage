package org.example.userservice.dto;

import lombok.Data;

@Data
public class UserEvent {
    private String operation;
    private String email;
    private String username;

    public UserEvent() {}

    public UserEvent(String operation, String email, String username) {
        this.operation = operation;
        this.email = email;
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "operation='" + operation + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

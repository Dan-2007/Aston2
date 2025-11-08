package org.example.task5_1.notification.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserEvent {
    public enum Operation { CREATE, DELETE }

    private final Operation operation;
    private final String email;

    @JsonCreator
    public UserEvent(@JsonProperty("operation") Operation operation,
                     @JsonProperty("email") String email) {
        this.operation = operation;
        this.email = email;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getEmail() {
        return email;
    }
}
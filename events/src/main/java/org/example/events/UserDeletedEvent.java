package org.example.events;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class UserDeletedEvent {
    private String eventId;
    private LocalDateTime eventTimestamp;
    private Long id;
    private String email;

    public UserDeletedEvent() {
    }

    private UserDeletedEvent(Long id, String email) {
        this.eventId = UUID.randomUUID().toString();
        this.eventTimestamp = LocalDateTime.now();
        this.id = id;
        this.email = email;
    }

    public String getEventId() { return eventId; }
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public Long getId() { return id; }
    public String getEmail() { return email; }

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public UserDeletedEvent build() {
            return new UserDeletedEvent(id, email);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDeletedEvent that = (UserDeletedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "UserDeletedEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", id=" + id +
                ", email='" + email + '\'' +
                '}';
    }
}
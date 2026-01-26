package dev.codedbydavid.eventhub.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {
    private final UUID id;
    private final String title;
    private final LocalDateTime startsAt;
    private final LocalDateTime endsAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Event(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.startsAt = builder.startsAt;
        this.endsAt = builder.endsAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void validate() {
        if (endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new EventValidationException(
                "endsAt must be after startsAt. startsAt: " + startsAt + ", endsAt: " + endsAt
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String title;
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder startsAt(LocalDateTime startsAt) {
            this.startsAt = startsAt;
            return this;
        }

        public Builder endsAt(LocalDateTime endsAt) {
            this.endsAt = endsAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }
}


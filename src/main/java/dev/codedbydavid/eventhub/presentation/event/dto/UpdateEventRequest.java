package dev.codedbydavid.eventhub.presentation.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateEventRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @FutureOrPresent(message = "startsAt must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startsAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endsAt;

    public UpdateEventRequest() {
    }

    public UpdateEventRequest(String title, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }
}


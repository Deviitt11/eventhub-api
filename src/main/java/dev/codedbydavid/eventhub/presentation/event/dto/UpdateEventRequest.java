package dev.codedbydavid.eventhub.presentation.event.dto;

//import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UpdateEventRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    //@FutureOrPresent(message = "startsAt must be in the present or future")
    private Instant startsAt;

    private Instant endsAt;

    public UpdateEventRequest() {
    }

    public UpdateEventRequest(String title, Instant startsAt, Instant endsAt) {
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

    public Instant getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }
}


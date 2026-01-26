package dev.codedbydavid.eventhub.domain.event;

public class EventValidationException extends RuntimeException {
    public EventValidationException(String message) {
        super(message);
    }
}


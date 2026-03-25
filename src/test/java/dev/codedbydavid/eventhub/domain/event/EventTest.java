package dev.codedbydavid.eventhub.domain.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void shouldThrowExceptionWhenStartsAtIsNull() {
        Event event = baseEventBuilder()
                .startsAt(null)
                .endsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .build();

        EventValidationException exception = assertThrows(
                EventValidationException.class,
                event::validate
        );

        assertEquals("startsAt is required", exception.getMessage());
    }

    @Test
    void shouldValidateSuccessfullyWhenEndsAtIsAfterStartsAt() {
        Event event = baseEventBuilder()
                .startsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .endsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .build();

        assertDoesNotThrow(event::validate);
    }

    @Test
    void shouldValidateSuccessfullyWhenEndsAtIsNull() {
        Event event = baseEventBuilder()
                .startsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .endsAt(null)
                .build();

        assertDoesNotThrow(event::validate);
    }

    @Test
    void shouldThrowExceptionWhenEndsAtIsBeforeStartsAt() {
        Event event = baseEventBuilder()
                .startsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .endsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .build();

        EventValidationException exception = assertThrows(
                EventValidationException.class,
                event::validate
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
    }

    @Test
    void shouldThrowExceptionWhenEndsAtEqualsStartsAt() {
        LocalDateTime sameTime = LocalDateTime.of(2024, 12, 20, 10, 0);
        Event event = baseEventBuilder()
                .startsAt(sameTime)
                .endsAt(sameTime)
                .build();

        EventValidationException exception = assertThrows(
                EventValidationException.class,
                event::validate
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
    }

    private Event.Builder baseEventBuilder() {
        return Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }
}


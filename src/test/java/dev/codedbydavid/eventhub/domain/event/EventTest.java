package dev.codedbydavid.eventhub.domain.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void shouldValidateSuccessfullyWhenEndsAtIsAfterStartsAt() {
        // Given
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .startsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .endsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When & Then
        assertDoesNotThrow(() -> event.validate());
    }

    @Test
    void shouldValidateSuccessfullyWhenEndsAtIsNull() {
        // Given
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .startsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .endsAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When & Then
        assertDoesNotThrow(() -> event.validate());
    }

    @Test
    void shouldThrowExceptionWhenEndsAtIsBeforeStartsAt() {
        // Given
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .startsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .endsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When & Then
        EventValidationException exception = assertThrows(
                EventValidationException.class,
                () -> event.validate()
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
    }

    @Test
    void shouldThrowExceptionWhenEndsAtEqualsStartsAt() {
        // Given
        LocalDateTime sameTime = LocalDateTime.of(2024, 12, 20, 10, 0);
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .startsAt(sameTime)
                .endsAt(sameTime)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When & Then
        EventValidationException exception = assertThrows(
                EventValidationException.class,
                () -> event.validate()
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
    }
}


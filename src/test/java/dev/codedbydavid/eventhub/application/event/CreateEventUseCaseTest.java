package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import dev.codedbydavid.eventhub.domain.event.EventValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEventUseCaseTest {

    @Mock
    private EventRepository eventRepository;

    private CreateEventUseCase createEventUseCase;

    @BeforeEach
    void setUp() {
        createEventUseCase = new CreateEventUseCase(eventRepository);
    }

    @Test
    void shouldCreateValidEvent() {
        // Given
        String title = "Test Event";
        LocalDateTime startsAt = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2024, 12, 20, 12, 0);

        Event savedEvent = Event.builder()
                .id(UUID.randomUUID())
                .title(title)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        Event result = createEventUseCase.execute(title, startsAt, endsAt);

        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(startsAt, result.getStartsAt());
        assertEquals(endsAt, result.getEndsAt());
        
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        
        Event capturedEvent = eventCaptor.getValue();
        assertEquals(title, capturedEvent.getTitle());
        assertEquals(startsAt, capturedEvent.getStartsAt());
        assertEquals(endsAt, capturedEvent.getEndsAt());
    }

    @Test
    void shouldRejectEventWhenEndsAtIsBeforeStartsAt() {
        // Given
        String title = "Test Event";
        LocalDateTime startsAt = LocalDateTime.of(2024, 12, 20, 12, 0);
        LocalDateTime endsAt = LocalDateTime.of(2024, 12, 20, 10, 0); // endsAt < startsAt

        // When & Then
        EventValidationException exception = assertThrows(
                EventValidationException.class,
                () -> createEventUseCase.execute(title, startsAt, endsAt)
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
    }

    @Test
    void shouldRejectEventWhenEndsAtEqualsStartsAt() {
        // Given
        String title = "Test Event";
        LocalDateTime startsAt = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2024, 12, 20, 10, 0); // endsAt == startsAt

        // When & Then
        EventValidationException exception = assertThrows(
                EventValidationException.class,
                () -> createEventUseCase.execute(title, startsAt, endsAt)
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
    }

    @Test
    void shouldCreateEventWithoutEndsAt() {
        // Given
        String title = "Test Event";
        LocalDateTime startsAt = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime endsAt = null; // endsAt is optional

        Event savedEvent = Event.builder()
                .id(UUID.randomUUID())
                .title(title)
                .startsAt(startsAt)
                .endsAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        Event result = createEventUseCase.execute(title, startsAt, endsAt);

        // Then
        assertNotNull(result);
        assertNull(result.getEndsAt());
        verify(eventRepository).save(any(Event.class));
    }
}


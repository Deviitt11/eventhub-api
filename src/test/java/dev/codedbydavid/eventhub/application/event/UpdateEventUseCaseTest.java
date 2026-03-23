package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventNotFoundException;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import dev.codedbydavid.eventhub.domain.event.EventValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEventUseCaseTest {

    @Mock
    private EventRepository eventRepository;

    private UpdateEventUseCase updateEventUseCase;

    @BeforeEach
    void setUp() {
        updateEventUseCase = new UpdateEventUseCase(eventRepository);
    }

    @Test
    void shouldThrowWhenEventDoesNotExist() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> updateEventUseCase.execute(eventId, "Updated title", LocalDateTime.of(2024, 12, 21, 10, 0), null)
        );

        assertEquals("Event not found with id: " + eventId, exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldKeepExistingValuesWhenNullMeansNoChange() {
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 1, 9, 0);
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 12, 2, 9, 0);
        LocalDateTime startsAt = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2024, 12, 20, 12, 0);
        Event existingEvent = Event.builder()
                .id(eventId)
                .title("Original title")
                .startsAt(startsAt)
                .endsAt(endsAt)
                .createdAt(createdAt)
                .updatedAt(originalUpdatedAt)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = updateEventUseCase.execute(eventId, null, null, null);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals(eventId, savedEvent.getId());
        assertEquals(existingEvent.getTitle(), savedEvent.getTitle());
        assertEquals(existingEvent.getStartsAt(), savedEvent.getStartsAt());
        assertEquals(existingEvent.getEndsAt(), savedEvent.getEndsAt());
        assertEquals(existingEvent.getCreatedAt(), savedEvent.getCreatedAt());
        assertNotNull(savedEvent.getUpdatedAt());
        assertTrue(savedEvent.getUpdatedAt().isAfter(originalUpdatedAt));

        assertEquals(savedEvent.getId(), result.getId());
        assertEquals(savedEvent.getTitle(), result.getTitle());
        assertEquals(savedEvent.getStartsAt(), result.getStartsAt());
        assertEquals(savedEvent.getEndsAt(), result.getEndsAt());
        assertEquals(savedEvent.getCreatedAt(), result.getCreatedAt());
        assertEquals(savedEvent.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtAndModifiedFields() {
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 1, 9, 0);
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 12, 2, 9, 0);
        Event existingEvent = Event.builder()
                .id(eventId)
                .title("Original title")
                .startsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .endsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .createdAt(createdAt)
                .updatedAt(originalUpdatedAt)
                .build();
        String newTitle = "Updated title";
        LocalDateTime newStartsAt = LocalDateTime.of(2024, 12, 21, 10, 0);
        LocalDateTime newEndsAt = LocalDateTime.of(2024, 12, 21, 13, 0);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = updateEventUseCase.execute(eventId, newTitle, newStartsAt, newEndsAt);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals(eventId, savedEvent.getId());
        assertEquals(newTitle, savedEvent.getTitle());
        assertEquals(newStartsAt, savedEvent.getStartsAt());
        assertEquals(newEndsAt, savedEvent.getEndsAt());
        assertEquals(createdAt, savedEvent.getCreatedAt());
        assertTrue(savedEvent.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals(savedEvent.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void shouldRejectUpdateWhenResultingEventIsInvalid() {
        UUID eventId = UUID.randomUUID();
        Event existingEvent = Event.builder()
                .id(eventId)
                .title("Original title")
                .startsAt(LocalDateTime.of(2024, 12, 20, 10, 0))
                .endsAt(LocalDateTime.of(2024, 12, 20, 12, 0))
                .createdAt(LocalDateTime.of(2024, 12, 1, 9, 0))
                .updatedAt(LocalDateTime.of(2024, 12, 2, 9, 0))
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        EventValidationException exception = assertThrows(
                EventValidationException.class,
                () -> updateEventUseCase.execute(
                        eventId,
                        null,
                        LocalDateTime.of(2024, 12, 21, 12, 0),
                        LocalDateTime.of(2024, 12, 21, 10, 0)
                )
        );

        assertTrue(exception.getMessage().contains("endsAt must be after startsAt"));
        verify(eventRepository, never()).save(any(Event.class));
    }
}

package dev.codedbydavid.eventhub.presentation.event;

import dev.codedbydavid.eventhub.application.event.CreateEventUseCase;
import dev.codedbydavid.eventhub.application.event.DeleteEventUseCase;
import dev.codedbydavid.eventhub.application.event.GetEventUseCase;
import dev.codedbydavid.eventhub.application.event.ListEventsUseCase;
import dev.codedbydavid.eventhub.application.event.UpdateEventUseCase;
import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.presentation.event.dto.CreateEventRequest;
import dev.codedbydavid.eventhub.presentation.event.dto.EventResponse;
import dev.codedbydavid.eventhub.presentation.event.dto.UpdateEventRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;
    private final ListEventsUseCase listEventsUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;

    public EventController(CreateEventUseCase createEventUseCase,
                          GetEventUseCase getEventUseCase,
                          ListEventsUseCase listEventsUseCase,
                          UpdateEventUseCase updateEventUseCase,
                          DeleteEventUseCase deleteEventUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.getEventUseCase = getEventUseCase;
        this.listEventsUseCase = listEventsUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event event = createEventUseCase.execute(
                request.getTitle(),
                request.getStartsAt(),
                request.getEndsAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID id) {
        Event event = getEventUseCase.execute(id);
        return ResponseEntity.ok(toResponse(event));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> listEvents() {
        List<Event> events = listEventsUseCase.execute();
        List<EventResponse> responses = events.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdateEventRequest request) {
        Event event = updateEventUseCase.execute(
                id,
                request.getTitle(),
                request.getStartsAt(),
                request.getEndsAt()
        );
        return ResponseEntity.ok(toResponse(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        deleteEventUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}


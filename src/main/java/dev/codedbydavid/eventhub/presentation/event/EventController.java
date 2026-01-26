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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Event management API")
public class EventController {

        private static final Logger log = LoggerFactory.getLogger(EventController.class);

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

        @Operation(summary = "Create a new event")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Event created successfully"),
                        @ApiResponse(responseCode = "400", description = "Validation error")
        })
        @PostMapping
        public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {

                Event event = createEventUseCase.execute(
                                request.getTitle(),
                                LocalDateTime.ofInstant(request.getStartsAt(), ZoneOffset.UTC),
                                LocalDateTime.ofInstant(request.getEndsAt(), ZoneOffset.UTC));
                return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(event));
        }

        @Operation(summary = "Get an event by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Event found"),
                        @ApiResponse(responseCode = "404", description = "Event not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<EventResponse> getEvent(@PathVariable UUID id) {
                Event event = getEventUseCase.execute(id);
                return ResponseEntity.ok(toResponse(event));
        }

        @Operation(summary = "List all events")
        @ApiResponse(responseCode = "200", description = "List of events")
        @GetMapping
        public ResponseEntity<List<EventResponse>> listEvents() {
                List<Event> events = listEventsUseCase.execute();
                List<EventResponse> responses = events.stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }

        @Operation(summary = "Update an event")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Validation error"),
                        @ApiResponse(responseCode = "404", description = "Event not found")
        })
        @PutMapping("/{id}")
        public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id,
                        @Valid @RequestBody UpdateEventRequest request) {
                Event event = updateEventUseCase.execute(
                                id,
                                request.getTitle(),
                                request.getStartsAt() != null
                                                ? LocalDateTime.ofInstant(request.getStartsAt(), ZoneOffset.UTC)
                                                : null,
                                request.getEndsAt() != null
                                                ? LocalDateTime.ofInstant(request.getEndsAt(), ZoneOffset.UTC)
                                                : null);
                return ResponseEntity.ok(toResponse(event));
        }

        @Operation(summary = "Delete an event")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Event not found")
        })
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteEvent(@PathVariable UUID id) {
                deleteEventUseCase.execute(id);
        }

        private EventResponse toResponse(Event event) {
                return new EventResponse(
                                event.getId(),
                                event.getTitle(),
                                event.getStartsAt(),
                                event.getEndsAt(),
                                event.getCreatedAt(),
                                event.getUpdatedAt());
        }
}

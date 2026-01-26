package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventNotFoundException;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateEventUseCase {
    private final EventRepository eventRepository;

    public UpdateEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event execute(UUID id, String title, LocalDateTime startsAt, LocalDateTime endsAt) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        Event.Builder builder = Event.builder()
                .id(existingEvent.getId())
                .title(title != null ? title : existingEvent.getTitle())
                .startsAt(startsAt != null ? startsAt : existingEvent.getStartsAt())
                .endsAt(endsAt != null ? endsAt : existingEvent.getEndsAt())
                .createdAt(existingEvent.getCreatedAt())
                .updatedAt(LocalDateTime.now());

        Event updatedEvent = builder.build();
        updatedEvent.validate();
        return eventRepository.save(updatedEvent);
    }
}


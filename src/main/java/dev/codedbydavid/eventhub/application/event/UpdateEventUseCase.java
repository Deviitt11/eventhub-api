package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventNotFoundException;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateEventUseCase {
    private final EventRepository eventRepository;
    private final Clock clock;

    @Autowired
    public UpdateEventUseCase(EventRepository eventRepository) {
        this(eventRepository, Clock.systemUTC());
    }

    UpdateEventUseCase(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
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
                .updatedAt(LocalDateTime.now(clock));

        Event updatedEvent = builder.build();
        updatedEvent.validate();
        return eventRepository.save(updatedEvent);
    }
}


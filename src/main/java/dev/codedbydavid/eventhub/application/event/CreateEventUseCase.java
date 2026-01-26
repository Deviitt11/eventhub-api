package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
//import java.util.UUID;

@Service
public class CreateEventUseCase {
    private final EventRepository eventRepository;

    public CreateEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event execute(String title, LocalDateTime startsAt, LocalDateTime endsAt) {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .id(null) // Let persistence generate the ID
                .title(title)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .createdAt(now)
                .updatedAt(now)
                .build();

        event.validate();
        return eventRepository.save(event);
    }
}


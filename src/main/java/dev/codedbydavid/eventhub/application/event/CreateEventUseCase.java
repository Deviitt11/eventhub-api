package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CreateEventUseCase {
    private final EventRepository eventRepository;
    private final Clock clock;

    @Autowired
    public CreateEventUseCase(EventRepository eventRepository) {
        this(eventRepository, Clock.systemUTC());
    }

    CreateEventUseCase(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    public Event execute(String title, LocalDateTime startsAt, LocalDateTime endsAt) {
        LocalDateTime now = LocalDateTime.now(clock);
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


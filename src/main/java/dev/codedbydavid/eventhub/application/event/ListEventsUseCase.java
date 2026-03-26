package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListEventsUseCase {
    private final EventRepository eventRepository;

    public ListEventsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> execute() {
        return eventRepository.findAll();
    }

    public List<Event> execute(LocalDateTime startsAtFrom, LocalDateTime startsAtTo) {
        return eventRepository.findAll(startsAtFrom, startsAtTo);
    }
}


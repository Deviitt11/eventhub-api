package dev.codedbydavid.eventhub.application.event;

import dev.codedbydavid.eventhub.domain.event.EventNotFoundException;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteEventUseCase {
    private final EventRepository eventRepository;

    public DeleteEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void execute(UUID id) {
        eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        eventRepository.deleteById(id);
    }
}


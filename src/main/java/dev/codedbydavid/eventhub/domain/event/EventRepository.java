package dev.codedbydavid.eventhub.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Event save(Event event);
    
    Optional<Event> findById(UUID id);
    
    List<Event> findAll();

    List<Event> findAll(LocalDateTime startsAtFrom, LocalDateTime startsAtTo);
    
    void deleteById(UUID id);
}


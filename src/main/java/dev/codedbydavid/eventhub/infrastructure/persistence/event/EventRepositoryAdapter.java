package dev.codedbydavid.eventhub.infrastructure.persistence.event;

import dev.codedbydavid.eventhub.domain.event.Event;
import dev.codedbydavid.eventhub.domain.event.EventNotFoundException;
import dev.codedbydavid.eventhub.domain.event.EventRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EventRepositoryAdapter implements EventRepository {
    private final EventJpaRepository jpaRepository;

    public EventRepositoryAdapter(EventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Event save(Event event) {
        // CREATE: let JPA generate the UUID
        if (event.getId() == null) {
            EventJpaEntity entity = new EventJpaEntity(
                    null,
                    event.getTitle(),
                    event.getStartsAt(),
                    event.getEndsAt(),
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            );
            EventJpaEntity saved = jpaRepository.save(entity); // persist
            return toDomainEntity(saved);
        }

        // UPDATE: must exist, otherwise 404
        if (!jpaRepository.existsById(event.getId())) {
            throw new EventNotFoundException(event.getId());
        }

        EventJpaEntity entity = toJpaEntity(event); // includes id
        EventJpaEntity saved = jpaRepository.save(entity); // merge
        return toDomainEntity(saved);
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public List<Event> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private EventJpaEntity toJpaEntity(Event event) {
        // For UPDATE we must keep the ID. For CREATE we bypass this method.
        return new EventJpaEntity(
                event.getId(),
                event.getTitle(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    private Event toDomainEntity(EventJpaEntity entity) {
        return Event.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

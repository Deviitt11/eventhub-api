package dev.codedbydavid.eventhub.infrastructure.persistence.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, UUID> {
    List<EventJpaEntity> findAllByOrderByStartsAtAscIdAsc();
    List<EventJpaEntity> findAllByStartsAtGreaterThanEqualOrderByStartsAtAscIdAsc(LocalDateTime startsAtFrom);
    List<EventJpaEntity> findAllByStartsAtLessThanEqualOrderByStartsAtAscIdAsc(LocalDateTime startsAtTo);
    List<EventJpaEntity> findAllByStartsAtBetweenOrderByStartsAtAscIdAsc(LocalDateTime startsAtFrom, LocalDateTime startsAtTo);
}


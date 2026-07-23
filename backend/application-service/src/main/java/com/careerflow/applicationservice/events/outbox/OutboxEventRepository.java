package com.careerflow.applicationservice.events.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(
        value = """
            SELECT * FROM outbox_events
            WHERE status = 'PENDING' AND attempts < :maxAttempts
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<OutboxEvent> findPendingForUpdate(
        @Param("batchSize") int batchSize,
        @Param("maxAttempts") int maxAttempts
    );
}

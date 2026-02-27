package hu.minthaka.orchestrator.repository;

import hu.minthaka.orchestrator.model.OrchestratorOutbox;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface OutboxRepository extends R2dbcRepository<OrchestratorOutbox, UUID> {

  @Query("SELECT * FROM outbox_orchestrator WHERE processed = false ORDER BY created_at ASC")
  Flux<OrchestratorOutbox> findByProcessedFalseOrderByCreatedAt();
}

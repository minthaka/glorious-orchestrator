package hu.minthaka.orchestrator.repository;

import hu.minthaka.orchestrator.model.Request;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RequestsRepository extends R2dbcRepository<Request, UUID> {
}

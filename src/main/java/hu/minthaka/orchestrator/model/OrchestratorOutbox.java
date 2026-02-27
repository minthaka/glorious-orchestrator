package hu.minthaka.orchestrator.model;

import hu.minthaka.common.model.AbstractPayloadOutboxMessage;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "outbox_orchestrator")
public class OrchestratorOutbox extends AbstractPayloadOutboxMessage {
}

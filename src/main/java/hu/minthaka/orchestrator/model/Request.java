package hu.minthaka.orchestrator.model;


import hu.minthaka.common.enums.Status;
import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "requests")
public class Request implements Persistable<UUID> {
  @Id
  @Column("request_id")
  private UUID requestId = UUID.randomUUID();

  private Status status;

  private Json payload;

  @Column("payload_hash")
  private String hash;

  @Column("last_updated")
  private LocalDateTime lastUpdated;

  @Version
  private Long version;

  @Transient // This is not saved to DB
  private boolean isNew = true;

  @Override
  @Transient
  public boolean isNew() {
    return isNew || requestId == null;
  }

  public void setNotNew() {
    this.isNew = false;
  }

  public UUID getId() {
    return requestId;
  }
}

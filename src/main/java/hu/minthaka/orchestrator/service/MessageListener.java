package hu.minthaka.orchestrator.service;

import hu.minthaka.common.dto.DoctorMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@EnableKafka
public class MessageListener {
  @Autowired
  private OrchestratorService service;

  @KafkaListener(topics = "doctor.answer", groupId = "health-orchestrator-group")
  public Mono<Void> listenToDoctors(DoctorMessageDTO doctor) {
    log.info("Received: {}", doctor);
    return switch (doctor.type()) {
      case DOCTOR_CREATED -> service.doctorCreated(doctor);
      case DOCTOR_CREATION_ERROR -> service.doctorCreationError(doctor);
      default -> {
        log.info("Irrelevant message caught: {}", doctor.type());
        yield Mono.empty();
      }
    };
  }
}

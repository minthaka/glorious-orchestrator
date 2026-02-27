package hu.minthaka.orchestrator.config;

import hu.minthaka.common.dto.DoctorDataCreateDTO;
import hu.minthaka.common.dto.DoctorDataDTO;
import hu.minthaka.common.dto.DoctorMessageDTO;
import hu.minthaka.common.dto.WorkingHourDTO;
import hu.minthaka.common.enums.Topic;
import hu.minthaka.orchestrator.repository.OutboxRepository;
import hu.minthaka.orchestrator.service.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.util.List;

@Slf4j
@Component
public class OutboxScheduler {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MessageSender messageSender;
  @Autowired
  private OutboxRepository repository;

  @Scheduled(fixedRate = 2000)
  public void emitOutboxMessages() {

    repository.findByProcessedFalseOrderByCreatedAt()
        .flatMap(orchestratorOutbox -> {
          DoctorDataCreateDTO doctorDataCreateDTO = objectMapper.readValue(
              orchestratorOutbox.getPayload().asString(),
              DoctorDataCreateDTO.class);
          List<WorkingHourDTO> workingHourDTOList = doctorDataCreateDTO.workingHours().stream()
              .map(item -> new WorkingHourDTO(null, null, item.from(), item.to())).toList();
          DoctorDataDTO doctorDataDTO = new DoctorDataDTO(null, doctorDataCreateDTO.firstName(),
              doctorDataCreateDTO.lastName(), doctorDataCreateDTO.email(), workingHourDTOList);

          DoctorMessageDTO message = new DoctorMessageDTO(orchestratorOutbox.getMessageID(),
              orchestratorOutbox.getCommand(), doctorDataDTO);

          Topic topic = Topic.INVALID;

          switch (orchestratorOutbox.getCommand()) {
            case CREATE_DOCTOR -> {
              topic = Topic.DOCTOR;
            }
          }

          messageSender.sendMessage(message, topic);
          orchestratorOutbox.setNotNew();
          orchestratorOutbox.setProcessed(true);
          return repository.save(orchestratorOutbox);
        }).subscribe();
  }
}

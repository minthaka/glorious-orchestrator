package hu.minthaka.orchestrator.service;

import hu.minthaka.common.dto.DoctorDataCreateDTO;
import hu.minthaka.common.dto.DoctorDataDTO;
import hu.minthaka.common.dto.DoctorMessageDTO;
import hu.minthaka.common.dto.WorkingHourDTO;
import hu.minthaka.common.enums.Command;
import hu.minthaka.common.enums.Status;
import hu.minthaka.common.utils.HashUtils;
import hu.minthaka.orchestrator.dto.RequestDTO;
import hu.minthaka.orchestrator.model.OrchestratorOutbox;
import hu.minthaka.orchestrator.model.Request;
import hu.minthaka.orchestrator.repository.OutboxRepository;
import hu.minthaka.orchestrator.repository.RequestsRepository;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {

  private final ObjectMapper objectMapper;
  private final OutboxRepository outboxRepository;
  private final RequestsRepository requestsRepository;
  private final HashService hashService;

  @Transactional
  public Mono<String> addDoctor(DoctorDataCreateDTO request) {
    String hash = HashUtils.generateHash(
        request.email() + request.lastName() + request.firstName() + request.workingHoursHash());

    return hashService.duplicate("/doctors/add", hash)
        .flatMap(duplicate -> {
          if (duplicate) {
            return Mono.just("The request is already sent. Please wait 5 minutes!");
          }
          Request requestData = new Request();
          requestData.setStatus(Status.INITIALIZATION);
          Json payloadJson = Json.of(objectMapper.writeValueAsString(request));
          requestData.setPayload(payloadJson);
          requestData.setHash(HashUtils.generateHash(
              request.email() + request.firstName() + request.lastName() +
                  request.workingHoursHash()));

          return requestsRepository.save(requestData)
              .flatMap(savedRequest -> {
                List<WorkingHourDTO> workingHourDTOList = new ArrayList<>();
                request.workingHours().stream()
                    .map(item -> new WorkingHourDTO(null, null, item.from(), item.to()))
                    .forEach(workingHourDTOList::add);
                DoctorDataDTO doctorDataCreateDTO = new DoctorDataDTO(null, request.firstName(),
                    request.lastName(), request.email(), workingHourDTOList);

                OrchestratorOutbox outbox = new OrchestratorOutbox();
                outbox.setCommand(Command.CREATE_DOCTOR);
                outbox.setCreatedAt(LocalDateTime.now());
                outbox.setMessageID(savedRequest.getId());
                Json doctorpayloadJson = Json.of(
                    objectMapper.writeValueAsString(doctorDataCreateDTO));
                outbox.setPayload(doctorpayloadJson);

                log.info("Doctor REQUEST SAVED: {}", outbox);

                return outboxRepository.save(outbox)
                    .map(savedOutbox -> savedOutbox.getId().toString());
              });
        });
  }

  public Mono<Void> doctorCreated(DoctorMessageDTO message) {

    log.info("Doctor is created: {}", message.payload());
    return requestsRepository.findById(message.messageID())
        .flatMap(request -> {
          request.setStatus(Status.SUCCESS);
          request.setNotNew();
          return requestsRepository.save(request);
        }).then();
  }

  public Mono<Void> doctorCreationError(DoctorMessageDTO message) {
    log.error("Doctor is not created: {}", message.payload());
    return requestsRepository.findById(message.messageID())
        .flatMap(request -> {
          request.setStatus(Status.FAILED);
          request.setNotNew();
          return requestsRepository.save(request);
        }).then();

  }

  public Flux<RequestDTO> findAll() {
    return requestsRepository.findAll()
        .map(request -> new RequestDTO(request.getId(), request.getStatus(),
            objectMapper.readValue(request.getPayload().asString(), DoctorDataCreateDTO.class)));
  }
}

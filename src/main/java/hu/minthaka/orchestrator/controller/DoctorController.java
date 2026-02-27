package hu.minthaka.orchestrator.controller;

import hu.minthaka.common.dto.DoctorDataCreateDTO;
import hu.minthaka.orchestrator.dto.RequestDTO;
import hu.minthaka.orchestrator.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
  @Autowired
  private OrchestratorService service;

  @PostMapping
  public Mono<String> addDoctor(@RequestBody DoctorDataCreateDTO request) {
    return service.addDoctor(request);
  }

  @GetMapping("/test")
  public Mono<String> test() {
    return Mono.just("Works");
  }

  @GetMapping("/all")
  public Flux<RequestDTO> findAll() {
    return service.findAll();
  }
}

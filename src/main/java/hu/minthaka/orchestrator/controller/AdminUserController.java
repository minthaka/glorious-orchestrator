package hu.minthaka.orchestrator.controller;

import hu.minthaka.common.dto.UserDataDTO;
import hu.minthaka.orchestrator.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

  @Autowired
  private AdminUserService service;

  @PostMapping
  public Mono<UUID> addNewUser(@RequestBody UserDataDTO userDataDTO) {
    return service.addUser(userDataDTO);
  }

  @GetMapping
  public Flux<UserDataDTO> listUsers() {
    return service.listUsers();
  }
}

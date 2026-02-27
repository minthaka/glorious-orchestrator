package hu.minthaka.orchestrator.service;

import hu.minthaka.common.dto.UserDataDTO;
import hu.minthaka.common.enums.Role;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
  @Autowired
  private Keycloak keycloak;
  private final String realm = "healthcare";
  private final String clientId = "healthcare-client";

  public Mono<UUID> addUser(UserDataDTO userDataDTO) {

    return Mono.fromCallable(() -> {
          UserRepresentation user = getUserRepresentation(userDataDTO);

          try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() != 201) {
              return ResponseEntity.status(response.getStatus()).body("User creation failed");
            }

            // 3. Extract the User ID from the Location Header
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // 4. Assign the 'role' Client Role
            // First, find the internal UUID of your client (different from clientId string)
            String clientUuid = keycloak.realm(realm).clients()
                .findByClientId(clientId).get(0).getId();

            // Get the RoleRepresentation for 'role' from that client
            List<RoleRepresentation> roles = new ArrayList<>();
            userDataDTO.roles().forEach(role -> roles.add(keycloak.realm(realm).clients()
                .get(clientUuid).roles().get(role.name()).toRepresentation()));

            // Map the role to the user
            keycloak.realm(realm).users().get(userId)
                .roles().clientLevel(clientUuid).add(roles);

            return UUID.fromString(userId);
          } catch (Exception e) {
            return null;
          }
        }).subscribeOn(Schedulers.boundedElastic())
        .flatMap(userId -> Mono.just((UUID) userId));
  }

  private static UserRepresentation getUserRepresentation(UserDataDTO request) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(request.username());
    user.setEmail(request.email());
    user.setEmailVerified(true);

    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEnabled(true);
    user.setAttributes(Map.of("favorite-color", List.of(request.favoriteColor())));

    CredentialRepresentation cred = new CredentialRepresentation();
    cred.setType(CredentialRepresentation.PASSWORD);
    cred.setValue(request.password());
    cred.setTemporary(false);
    user.setCredentials(List.of(cred));
    return user;
  }

  public Flux<UserDataDTO> listUsers() {
    return Flux.defer(() -> Flux.fromIterable(keycloak.realm(realm).users().list()))
        .subscribeOn(
            Schedulers.boundedElastic()) // CRITICAL: Moves blocking I/O off the netty threads
        .map(userRepresentation -> {
          String internalClientId = keycloak.realm(realm)
              .clients()
              .findByClientId("healthcare-client")
              .get(0)
              .getId();
          UserResource userResource = keycloak.realm(realm).users().get(userRepresentation.getId());


          List<Role> userRoles = userResource.roles().clientLevel(internalClientId)
              .listEffective()
              .stream()
              .map(roleRep -> Role.getValue(roleRep.getName())) // Improved mapping
              .toList();

          return new UserDataDTO(
              UUID.fromString(userRepresentation.getId()),
              userRepresentation.getUsername(),
              "******",
              userRepresentation.getFirstName(),
              userRepresentation.getLastName(),
              userRepresentation.getEmail(),
              userRepresentation.getAttributes().get("favorite-color").get(0),
              userRoles
          );
        })
        .doOnError(er -> log.error("Keycloak user list error: {}", er.getMessage()));
  }
}

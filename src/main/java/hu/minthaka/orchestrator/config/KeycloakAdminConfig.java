package hu.minthaka.orchestrator.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {
  @Bean
  public Keycloak keycloakAdmin() {
    return KeycloakBuilder.builder()
        .serverUrl("http://localhost:8118")
        .realm("healthcare")
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId("healthcare-client")
        .clientSecret("GQTUJsRbJyfTINMTEMQMCyHp0ybry0tj")
        .build();
  }
}

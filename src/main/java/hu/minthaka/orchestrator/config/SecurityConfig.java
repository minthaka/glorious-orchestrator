package hu.minthaka.orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/admin/**").hasRole("ADMIN")
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
        )
        .csrf(ServerHttpSecurity.CsrfSpec::disable);

    return http.build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    // Must match .claim("roles", roles) in your JwtProvider
    authoritiesConverter.setAuthoritiesClaimName("roles");

    // Since Gateway provides [ROLE_MANAGER], we use empty prefix
    authoritiesConverter.setAuthorityPrefix("");

    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    // 1. Point to the ACTUAL physical location of the keys
    String jwksUri = "http://localhost:8787/.x114534/jwks.json";
    NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwksUri).build();

    // 2. Create a validator for your CUSTOM internal issuer name
    JwtIssuerValidator issuerValidator = new JwtIssuerValidator("healthcare-gateway-service");
    JwtTimestampValidator timestampValidator = new JwtTimestampValidator();

    // 3. Combine them
    var validator = new DelegatingOAuth2TokenValidator<>(timestampValidator, issuerValidator);

    jwtDecoder.setJwtValidator(validator);
    return jwtDecoder;
  }
}

package com.vitalbite.documental.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalbite.documental.infrastructure.security.JwtAuthoritiesConverter;
import com.vitalbite.documental.application.port.out.AuditoriaPort;
import org.slf4j.MDC;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;
import java.time.Instant;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper,
                                           AuditoriaPort auditoria)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/v3/api-docs",
                                "/actuator/health", "/").permitAll()
                        .requestMatchers("/auditoria/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, exception) ->
                                writeProblem(response, objectMapper, 401,
                                        "Autenticación requerida",
                                        "Debe proporcionar un token válido"))
                        .accessDeniedHandler((request, response, exception) -> {
                            auditoria.registrar(new AuditoriaPort.Registro(null, null,
                                    request.getRequestURI(), null, "DENEGADO", Instant.now(),
                                    MDC.get("correlationId"), request.getHeader("X-Saga-Id"),
                                    request.getRemoteAddr()));
                            writeProblem(response, objectMapper, 403,
                                    "Acceso denegado", "No tiene permisos para este recurso");
                        }))
                .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> new JwtAuthoritiesConverter()
                .convert(jwt).getAuthorities());
        return converter;
    }

    private void writeProblem(HttpServletResponse response, ObjectMapper objectMapper,
                              int status, String title, String detail) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "type", "about:blank", "title", title, "status", status, "detail", detail));
    }
}

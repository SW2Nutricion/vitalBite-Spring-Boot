package com.vitalbite.documental.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;

@Configuration
class JwtSecurityConfiguration {

    @Bean
    @Profile("!test")
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        var decoder = JwtDecoders.fromIssuerLocation(issuer);
        if (decoder instanceof org.springframework.security.oauth2.jwt.NimbusJwtDecoder nimbus) {
            nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(issuer),
                    new JwtAudienceValidator(),
                    new JwtTenantValidator()));
        }
        return decoder;
    }

    @Bean
    @Profile("test")
    JwtDecoder testJwtDecoder() {
        return token -> {
            throw new org.springframework.security.oauth2.server.resource.InvalidBearerTokenException(
                    "El perfil test no valida tokens reales");
        };
    }
}

package com.vitalbite.documental.infrastructure.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

final class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR =
            new OAuth2Error("invalid_token", "aud debe incluir vitalbite-documental", null);

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        return jwt.getAudience().contains("vitalbite-documental")
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(ERROR);
    }
}

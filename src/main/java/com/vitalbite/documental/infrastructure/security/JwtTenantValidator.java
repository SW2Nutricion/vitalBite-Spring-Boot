package com.vitalbite.documental.infrastructure.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

final class JwtTenantValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR =
            new OAuth2Error("invalid_token", "tenant_id es obligatorio para este rol", null);

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        boolean requiresTenant = SecurityClaims.roles(jwt).stream()
                .map(String::toUpperCase)
                .map(role -> role.replaceFirst("^ROLE_", ""))
                .anyMatch(role -> role.equals("NUTRICIONISTA") || role.equals("PACIENTE"));
        if (requiresTenant && (jwt.getClaimAsString("tenant_id") == null
                || jwt.getClaimAsString("tenant_id").isBlank())) {
            return OAuth2TokenValidatorResult.failure(ERROR);
        }
        return OAuth2TokenValidatorResult.success();
    }
}

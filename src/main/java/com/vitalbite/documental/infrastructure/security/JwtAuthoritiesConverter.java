package com.vitalbite.documental.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

public final class JwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Set<String> SUPPORTED_ROLES =
            Set.of("ADMIN", "NUTRICIONISTA", "PACIENTE", "SERVICE");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var authorities = SecurityClaims.roles(jwt).stream()
                .map(String::toUpperCase)
                .map(role -> role.replaceFirst("^ROLE_", ""))
                .filter(SUPPORTED_ROLES::contains)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
        var token = new org.springframework.security.oauth2.server.resource.authentication
                .JwtAuthenticationToken(jwt, authorities);
        return token;
    }
}

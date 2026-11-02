package com.vitalbite.documental.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

final class SecurityClaims {

    private SecurityClaims() {
    }

    static List<String> roles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> map
                && map.get("roles") instanceof Collection<?> values) {
            return values.stream().map(Object::toString).toList();
        }
        Object roles = jwt.getClaims().get("roles");
        if (roles instanceof Collection<?> values) {
            return values.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}

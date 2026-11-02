package com.vitalbite.documental.infrastructure.security;

import com.vitalbite.documental.application.port.out.UsuarioActualPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Component
public class SecurityUserAdapter implements UsuarioActualPort {

    @Override
    public UsuarioActual actual() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new SecurityException("Usuario autenticado no disponible");
        }
        String role = token.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElseThrow(() -> new SecurityException("El token no contiene un rol válido"));
        String subject = token.getToken().getSubject();
        MDC.put("userId", subject);
        return new UsuarioActual(subject, role,
                token.getToken().getClaimAsString("tenant_id"));
    }
}

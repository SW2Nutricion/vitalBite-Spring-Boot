package com.vitalbite.documental.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Service-Key";

    @Value("${internal.service-key:}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean publicEndpoint = path.endsWith("/actuator/health")
                || path.startsWith(request.getContextPath() + "/swagger-ui")
                || path.startsWith(request.getContextPath() + "/v3/api-docs");

        if (publicEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String receivedKey = request.getHeader(HEADER);
        if (expectedKey.isBlank() || receivedKey == null
                || !MessageDigest.isEqual(
                expectedKey.getBytes(StandardCharsets.UTF_8),
                receivedKey.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credencial interna invalida");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("api-gateway", null, java.util.List.of()));
        filterChain.doFilter(request, response);
    }
}

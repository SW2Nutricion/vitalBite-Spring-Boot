package com.vitalbite.documental.infrastructure.config;

import com.vitalbite.documental.application.exception.ArchivoExcedeLimiteException;
import com.vitalbite.documental.application.exception.ContenidoIAInvalidoException;
import com.vitalbite.documental.application.exception.DocumentoNoEncontrado;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import com.vitalbite.documental.application.port.out.AuditoriaPort;
import org.slf4j.MDC;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentoNoEncontrado.class)
    public ProblemDetail handleNotFound(DocumentoNoEncontrado exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Solicitud inválida");
        problem.setDetail("Uno o más campos no cumplen las reglas de validación");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Solicitud inválida");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud");
        problem.setTitle("Error interno");
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleConflict(IllegalStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Conflicto de operación");
        return problem;
    }

    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleForbidden(SecurityException exception,
                                         HttpServletRequest request,
                                         AuditoriaPort auditoria) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String sub = authentication == null ? null : authentication.getName();
        String rol = authentication == null ? null : authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).orElse(null);
        auditoria.registrar(new AuditoriaPort.Registro(sub, rol, request.getRequestURI(), null,
                "DENEGADO", Instant.now(), MDC.get("correlationId"),
                request.getHeader("X-Saga-Id"), request.getRemoteAddr()));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, exception.getMessage());
        problem.setTitle("Acceso denegado");
        return problem;
    }

    @ExceptionHandler(ContenidoIAInvalidoException.class)
    public ProblemDetail handleUnprocessable(ContenidoIAInvalidoException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
        problem.setTitle("Contenido de IA inválido");
        return problem;
    }

    @ExceptionHandler(ArchivoExcedeLimiteException.class)
    public ProblemDetail handleTooLarge(ArchivoExcedeLimiteException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONTENT_TOO_LARGE, exception.getMessage());
        problem.setTitle("Archivo demasiado grande");
        return problem;
    }
}

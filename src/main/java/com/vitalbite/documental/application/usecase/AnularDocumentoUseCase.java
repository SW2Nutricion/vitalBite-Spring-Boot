package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.application.port.out.IdempotenciaPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalbite.documental.domain.model.Documento;

import java.util.UUID;

public final class AnularDocumentoUseCase {

    private final DocumentoRepositoryPort repository;
    private final IdempotenciaPort idempotencia;
    private final ObjectMapper objectMapper;

    public AnularDocumentoUseCase(DocumentoRepositoryPort repository,
                                  IdempotenciaPort idempotencia,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.idempotencia = idempotencia;
        this.objectMapper = objectMapper;
    }

    public DocumentoResponseDTO execute(UUID id, String usuarioId, String rol,
                                        String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key es obligatorio");
        }
        String requestHash;
        try {
            requestHash = java.util.Base64.getEncoder().encodeToString(
                    java.security.MessageDigest.getInstance("SHA-256").digest(
                            objectMapper.writeValueAsBytes(java.util.Map.of(
                                    "id", id, "usuarioId", usuarioId, "rol", rol))));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo calcular el hash", exception);
        }
        IdempotenciaPort.RegistroIdempotencia previous = idempotencia.find(idempotencyKey)
                .orElse(null);
        if (previous != null) {
            if (!previous.requestHash().equals(requestHash)) {
                throw new IllegalStateException(
                        "La clave de idempotencia ya fue usada con otro cuerpo");
            }
            return previous.response();
        }
        Documento documento = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        if (!"ADMIN".equals(rol)
                && !documento.puedeConsultar(usuarioId, rol)) {
            throw new SecurityException("El usuario no puede anular este documento");
        }
        documento.anular();
        repository.save(documento);
        DocumentoResponseDTO response = new DocumentoResponseDTO(
                documento.id(), documento.tipo(), documento.estado(),
                documento.pacienteId(), documento.origenContenido(),
                documento.formato(), documento.tamanoBytes(),
                documento.hashSha256(), documento.version(),
                documento.creadoEn(), documento.anuladoEn());
        idempotencia.save(idempotencyKey, requestHash, response);
        return response;
    }
}

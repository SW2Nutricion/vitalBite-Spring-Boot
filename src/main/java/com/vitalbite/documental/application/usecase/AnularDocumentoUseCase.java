package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.domain.model.Documento;

import java.util.UUID;

public final class AnularDocumentoUseCase {

    private final DocumentoRepositoryPort repository;

    public AnularDocumentoUseCase(DocumentoRepositoryPort repository) {
        this.repository = repository;
    }

    public DocumentoResponseDTO execute(UUID id, String usuarioId, String rol,
                                        String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key es obligatorio");
        }
        Documento documento = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        if (!"ADMIN".equals(rol)
                && !documento.puedeConsultar(usuarioId, rol)) {
            throw new SecurityException("El usuario no puede anular este documento");
        }
        documento.anular();
        repository.save(documento);
        return new DocumentoResponseDTO(
                documento.id(), documento.tipo(), documento.estado(),
                documento.pacienteId(), documento.origenContenido(),
                documento.formato(), documento.tamanoBytes(),
                documento.hashSha256(), documento.version(),
                documento.creadoEn(), documento.anuladoEn());
    }
}

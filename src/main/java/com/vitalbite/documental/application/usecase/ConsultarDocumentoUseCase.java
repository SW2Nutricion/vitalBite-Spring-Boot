package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.application.port.out.UsuarioActualPort.UsuarioActual;
import com.vitalbite.documental.domain.model.Documento;
import com.vitalbite.documental.application.exception.DocumentoNoEncontrado;

import java.util.UUID;

public final class ConsultarDocumentoUseCase {

    private final DocumentoRepositoryPort repository;

    public ConsultarDocumentoUseCase(DocumentoRepositoryPort repository) {
        this.repository = repository;
    }

    public DocumentoResponseDTO execute(UUID id, UsuarioActual usuario) {
        Documento documento = repository.findById(id)
                .orElseThrow(DocumentoNoEncontrado::new);
        if (!documento.puedeConsultar(usuario.id(), usuario.rol(), usuario.tenantId())) {
            throw new SecurityException("El usuario no puede consultar este documento");
        }
        return new DocumentoResponseDTO(
                documento.id(), documento.tipo(), documento.estado(),
                documento.pacienteId(), documento.origenContenido(),
                documento.formato(), documento.tamanoBytes(),
                documento.hashSha256(), documento.version(),
                documento.creadoEn(), documento.anuladoEn(), documento.tenantId());
    }
}

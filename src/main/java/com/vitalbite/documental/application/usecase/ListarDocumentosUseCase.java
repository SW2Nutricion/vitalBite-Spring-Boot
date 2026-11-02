package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.application.port.out.UsuarioActualPort.UsuarioActual;
import com.vitalbite.documental.domain.model.Documento;
import com.vitalbite.documental.domain.model.EstadoDocumento;
import com.vitalbite.documental.domain.model.TipoDocumento;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public final class ListarDocumentosUseCase {

    private static final int MAX_SIZE = 100;
    private final DocumentoRepositoryPort repository;

    public ListarDocumentosUseCase(DocumentoRepositoryPort repository) {
        this.repository = repository;
    }

    public PaginaDocumentos execute(UsuarioActual usuario, String pacienteId,
                                    TipoDocumento tipo, EstadoDocumento estado,
                                    Instant desde, Instant hasta, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("page debe ser >= 0 y size debe estar entre 1 y 100");
        }
        List<Documento> filtrados = repository.findAll().stream()
                .filter(documento -> documento.puedeConsultar(usuario.id(), usuario.rol(),
                        usuario.tenantId()))
                .filter(documento -> pacienteId == null || pacienteId.equals(documento.pacienteId()))
                .filter(documento -> tipo == null || tipo == documento.tipo())
                .filter(documento -> estado == null || estado == documento.estado())
                .filter(documento -> desde == null || !documento.creadoEn().isBefore(desde))
                .filter(documento -> hasta == null || !documento.creadoEn().isAfter(hasta))
                .sorted(Comparator.comparing(Documento::creadoEn).reversed())
                .toList();
        int from = Math.min(page * size, filtrados.size());
        int to = Math.min(from + size, filtrados.size());
        return new PaginaDocumentos(filtrados.subList(from, to).stream()
                .map(this::response).toList(), page, size, filtrados.size());
    }

    private DocumentoResponseDTO response(Documento documento) {
        return new DocumentoResponseDTO(documento.id(), documento.tipo(), documento.estado(),
                documento.pacienteId(), documento.origenContenido(), documento.formato(),
                documento.tamanoBytes(), documento.hashSha256(), documento.version(),
                documento.creadoEn(), documento.anuladoEn(), documento.tenantId());
    }

    public record PaginaDocumentos(List<DocumentoResponseDTO> contenido,
                                   int page, int size, int totalElements) {
    }
}

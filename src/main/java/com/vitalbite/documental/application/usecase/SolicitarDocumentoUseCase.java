package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.exception.ArchivoExcedeLimiteException;
import com.vitalbite.documental.application.exception.ContenidoIAInvalidoException;
import com.vitalbite.documental.application.dto.MetadatosIADTO;
import com.vitalbite.documental.application.dto.SolicitudDocumentoDTO;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.application.port.out.GeneradorDocumentoPort;
import com.vitalbite.documental.domain.model.AclDocumento;
import com.vitalbite.documental.domain.model.Documento;
import com.vitalbite.documental.domain.model.MetadatosIA;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SolicitarDocumentoUseCase {

    private final DocumentoRepositoryPort repository;
    private final GeneradorDocumentoPort generator;
    private final long maxFileBytes;
    private final Map<String, ResultadoIdempotente> idempotency = new ConcurrentHashMap<>();

    public SolicitarDocumentoUseCase(DocumentoRepositoryPort repository,
                                     GeneradorDocumentoPort generator,
                                     long maxFileBytes) {
        this.repository = repository;
        this.generator = generator;
        this.maxFileBytes = maxFileBytes;
    }

    public DocumentoResponseDTO execute(
            SolicitudDocumentoDTO request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key es obligatorio");
        }
        if (request == null || request.tipo() == null
                || request.pacienteId() == null || request.propietarioId() == null
                || request.datos() == null || request.formato() == null) {
            throw new IllegalArgumentException(
                    "tipo, pacienteId, propietarioId, datos y formato son obligatorios");
        }
        if (request.interpretacionIA() != null
                && !request.interpretacionIA().isBlank()
                && (request.metadatosIA() == null
                || request.metadatosIA().modelo() == null
                || request.metadatosIA().modelo().isBlank()
                || request.metadatosIA().version() == null
                || request.metadatosIA().version().isBlank()
                || request.metadatosIA().fuentes() == null
                || request.metadatosIA().fuentes().isEmpty())) {
            throw new ContenidoIAInvalidoException(
                    "interpretacionIA requiere modelo, version y fuentes");
        }
        String requestHash = hash(request.toString());
        ResultadoIdempotente previous = idempotency.get(idempotencyKey);
        if (previous != null) {
            if (!previous.requestHash().equals(requestHash)) {
                throw new IllegalStateException(
                        "La clave de idempotencia ya fue usada con otro cuerpo");
            }
            return previous.response();
        }

        Documento documento = new Documento(
                null,
                request.tipo(),
                request.pacienteId(),
                new AclDocumento(
                        request.propietarioId(),
                        request.pacienteId(),
                        request.compartidoCon()),
                request.origenContenido(),
                toDomain(request.metadatosIA()),
                request.formato());
        byte[] content = generator.generar(request);
        if (content.length > maxFileBytes) {
            documento.marcarFallido();
            throw new ArchivoExcedeLimiteException(
                    "El archivo excede el tamaño máximo permitido");
        }
        documento.marcarGenerado(content.length, hash(content));
        repository.save(documento);
        DocumentoResponseDTO response = response(documento);
        idempotency.putIfAbsent(
                idempotencyKey, new ResultadoIdempotente(requestHash, response));
        return response;
    }

    private MetadatosIA toDomain(MetadatosIADTO dto) {
        return dto == null ? null : new MetadatosIA(dto.modelo(), dto.version(), dto.fuentes());
    }

    private DocumentoResponseDTO response(Documento documento) {
        return new DocumentoResponseDTO(
                documento.id(), documento.tipo(), documento.estado(),
                documento.pacienteId(), documento.origenContenido(),
                documento.formato(), documento.tamanoBytes(),
                documento.hashSha256(), documento.version(),
                documento.creadoEn(), documento.anuladoEn());
    }

    private String hash(Object value) {
        return hash(value.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String hash(byte[] value) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo calcular el hash", exception);
        }
    }

    private record ResultadoIdempotente(
            String requestHash, DocumentoResponseDTO response) {
    }
}

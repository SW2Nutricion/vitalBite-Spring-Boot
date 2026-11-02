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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vitalbite.documental.application.port.out.IdempotenciaPort;
import com.vitalbite.documental.application.port.out.AlmacenArchivoPort;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public final class SolicitarDocumentoUseCase {

    private final DocumentoRepositoryPort repository;
    private final GeneradorDocumentoPort generator;
    private final IdempotenciaPort idempotencia;
    private final AlmacenArchivoPort almacen;
    private final ObjectMapper objectMapper;
    private final long maxFileBytes;

    public SolicitarDocumentoUseCase(DocumentoRepositoryPort repository,
                                     GeneradorDocumentoPort generator,
                                     IdempotenciaPort idempotencia,
                                     AlmacenArchivoPort almacen,
                                     ObjectMapper objectMapper,
                                     long maxFileBytes) {
        this.repository = repository;
        this.generator = generator;
        this.idempotencia = idempotencia;
        this.almacen = almacen;
        this.objectMapper = objectMapper;
        this.maxFileBytes = maxFileBytes;
    }

    public DocumentoResponseDTO execute(
            SolicitudDocumentoDTO request, String idempotencyKey) {
        return execute(request, idempotencyKey, null);
    }

    public DocumentoResponseDTO execute(
            SolicitudDocumentoDTO request, String idempotencyKey, String sagaId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key es obligatorio");
        }
        if (request == null || request.tipo() == null
                || request.propietarioId() == null
                || request.datos() == null || request.formato() == null) {
            throw new IllegalArgumentException(
                    "tipo, pacienteId, propietarioId, datos y formato son obligatorios");
        }
        if (request.pacienteId() == null && request.tipo() != com.vitalbite.documental.domain.model.TipoDocumento.FACTURA) {
            throw new IllegalArgumentException("pacienteId es obligatorio para este tipo de documento");
        }
        if (request.tipo() == com.vitalbite.documental.domain.model.TipoDocumento.FACTURA
                && request.tenantId() == null) {
            throw new IllegalArgumentException("tenantId es obligatorio para una factura");
        }
        if (request.origenContenido() != com.vitalbite.documental.domain.model.OrigenContenido.REGISTRADO
                && (request.metadatosIA() == null
                || request.metadatosIA().modelo() == null
                || request.metadatosIA().modelo().isBlank()
                || request.metadatosIA().version() == null
                || request.metadatosIA().version().isBlank()
                || request.metadatosIA().fuentes() == null
                || request.metadatosIA().fuentes().isEmpty())) {
            throw new ContenidoIAInvalidoException(
                    "contenido generado o mixto requiere modelo, version y fuentes");
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
        String requestHash = canonicalHash(request);
        idempotencia.purgeExpired();
        IdempotenciaPort.RegistroIdempotencia previous =
                idempotencia.reserve(idempotencyKey, requestHash).orElse(null);
        if (previous != null) {
            if (!previous.requestHash().equals(requestHash)) {
                throw new IllegalStateException(
                        "La clave de idempotencia ya fue usada con otro cuerpo");
            }
            for (int attempt = 0; attempt < 50; attempt++) {
                if (previous.response() != null) {
                    return previous.response();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrumpido al esperar idempotencia",
                            exception);
                }
                previous = idempotencia.find(idempotencyKey).orElseThrow();
            }
            throw new IllegalStateException("La operación idempotente no finalizó a tiempo");
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
                request.formato(), request.tenantId());
        documento.asignarSagaId(sagaId);
        byte[] content = generator.generar(request);
        if (content.length > maxFileBytes) {
            documento.marcarFallido();
            throw new ArchivoExcedeLimiteException(
                    "El archivo excede el tamaño máximo permitido");
        }
        repository.save(documento);
        try {
            almacen.guardar(documento.id(), content);
            documento.marcarGenerado(content.length, hash(content));
            repository.save(documento);
        } catch (RuntimeException exception) {
            documento.marcarFallido();
            repository.save(documento);
            throw new IllegalStateException("No se pudo almacenar el contenido del documento",
                    exception);
        }
        DocumentoResponseDTO response = response(documento);
        idempotencia.save(idempotencyKey, requestHash, response);
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
                documento.creadoEn(), documento.anuladoEn(), documento.tenantId());
    }

    private String canonicalHash(Object value) {
        try {
            JsonNode tree = canonicalize(objectMapper.valueToTree(value));
            return hash(objectMapper.writeValueAsBytes(tree));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo serializar el cuerpo", exception);
        }
    }

    private String hash(byte[] value) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo calcular el hash", exception);
        }
    }

    private JsonNode canonicalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
            Map<String, JsonNode> values = new java.util.TreeMap<>();
            fields.forEachRemaining(field -> values.put(field.getKey(),
                    canonicalize(field.getValue())));
            values.forEach(sorted::set);
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode sorted = objectMapper.createArrayNode();
            ArrayList<JsonNode> values = new ArrayList<>();
            node.forEach(value -> values.add(canonicalize(value)));
            values.sort(Comparator.comparing(JsonNode::toString));
            values.forEach(sorted::add);
            return sorted;
        }
        return node;
    }
}

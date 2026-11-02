package com.vitalbite.documental.infrastructure.in.web;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.dto.SolicitudDocumentoDTO;
import com.vitalbite.documental.application.usecase.AnularDocumentoUseCase;
import com.vitalbite.documental.application.usecase.ConsultarDocumentoUseCase;
import com.vitalbite.documental.application.usecase.SolicitarDocumentoUseCase;
import com.vitalbite.documental.application.usecase.DescargarDocumentoUseCase;
import com.vitalbite.documental.application.usecase.ListarDocumentosUseCase;
import com.vitalbite.documental.domain.model.EstadoDocumento;
import com.vitalbite.documental.domain.model.TipoDocumento;
import com.vitalbite.documental.application.port.out.UsuarioActualPort;
import com.vitalbite.documental.application.port.out.UsuarioActualPort.UsuarioActual;
import com.vitalbite.documental.application.port.out.AuditoriaPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;
import java.time.Instant;
import org.slf4j.MDC;

@RestController
@RequestMapping("/documentos")
@Tag(name = "Gestión documental", description = "Documentos generados y su ciclo de vida")
public class DocumentManagementController {

    private final SolicitarDocumentoUseCase solicitar;
    private final ConsultarDocumentoUseCase consultar;
    private final AnularDocumentoUseCase anular;
    private final UsuarioActualPort usuarioActual;
    private final DescargarDocumentoUseCase descargar;
    private final ListarDocumentosUseCase listar;
    private final AuditoriaPort auditoria;

    public DocumentManagementController(
            SolicitarDocumentoUseCase solicitar,
            ConsultarDocumentoUseCase consultar,
            AnularDocumentoUseCase anular, UsuarioActualPort usuarioActual,
            DescargarDocumentoUseCase descargar, ListarDocumentosUseCase listar,
            AuditoriaPort auditoria) {
        this.solicitar = solicitar;
        this.consultar = consultar;
        this.anular = anular;
        this.usuarioActual = usuarioActual;
        this.descargar = descargar;
        this.listar = listar;
        this.auditoria = auditoria;
    }

    @GetMapping("/{id}/contenido")
    @Operation(summary = "Descargar contenido del documento")
    public ResponseEntity<byte[]> content(@PathVariable UUID id,
                                          @RequestHeader(value = HttpHeaders.RANGE,
                                                  required = false) String range,
                                          @RequestHeader(value = HttpHeaders.IF_NONE_MATCH,
                                                  required = false) String ifNoneMatch) {
        DescargarDocumentoUseCase.ArchivoDocumento archivo =
                descargar.execute(id, usuarioActual.actual());
        String etag = "\"" + archivo.etag() + "\"";
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        registrar("DESCARGAR", id, "OK");
        byte[] content = archivo.contenido();
        int start = 0;
        int end = content.length - 1;
        HttpStatus status = HttpStatus.OK;
        if (range != null && range.startsWith("bytes=") && !range.contains(",")) {
            String[] bounds = range.substring(6).split("-", 2);
            try {
                start = Integer.parseInt(bounds[0]);
                if (bounds.length > 1 && !bounds[1].isBlank()) {
                    end = Integer.parseInt(bounds[1]);
                }
                if (start < 0 || start > end || end >= content.length) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .eTag(etag).build();
                }
                content = java.util.Arrays.copyOfRange(content, start, end + 1);
                status = HttpStatus.PARTIAL_CONTENT;
            } catch (NumberFormatException exception) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .eTag(etag).build();
            }
        }
        ResponseEntity.BodyBuilder response = ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.filename() + "\"")
                .eTag(etag).contentLength(content.length);
        if (status == HttpStatus.PARTIAL_CONTENT) {
            response.header(HttpHeaders.CONTENT_RANGE,
                    "bytes " + start + "-" + end + "/" + archivo.contenido().length);
        }
        return response.body(content);
    }

    @GetMapping
    @Operation(summary = "Listar documentos accesibles")
    public ResponseEntity<ListarDocumentosUseCase.PaginaDocumentos> list(
            @RequestParam(required = false) String pacienteId,
            @RequestParam(required = false) TipoDocumento tipo,
            @RequestParam(required = false) EstadoDocumento estado,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var response = listar.execute(usuarioActual.actual(), pacienteId, tipo,
                estado, desde, hasta, page, size);
        registrar("LISTAR", null, "OK");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Solicitar generación de documento")
    public ResponseEntity<DocumentoResponseDTO> create(
            @Valid @RequestBody SolicitudDocumentoDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey,
            @RequestHeader(value = "X-Saga-Id", required = false) String sagaId) {
        UsuarioActual usuario = usuarioActual.actual();
        DocumentoResponseDTO response = solicitar.execute(asignarIdentidad(request, usuario),
                idempotencyKey, sagaId);
        registrar("CREAR", response.id(), "OK");
        return ResponseEntity.status(201)
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar metadatos y estado del documento")
    public ResponseEntity<DocumentoResponseDTO> get(
            @PathVariable UUID id) {
        DocumentoResponseDTO response = consultar.execute(id, usuarioActual.actual());
        registrar("CONSULTAR", id, "OK");
        return ResponseEntity.ok()
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular documento como compensación de Saga")
    public ResponseEntity<DocumentoResponseDTO> cancel(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey) {
        DocumentoResponseDTO response =
                anular.execute(id, usuarioActual.actual(), idempotencyKey);
        registrar("ANULAR", id, "OK");
        return ResponseEntity.ok()
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }

    private void registrar(String accion, UUID documentoId, String resultado) {
        UsuarioActual usuario = usuarioActual.actual();
        auditoria.registrar(new AuditoriaPort.Registro(usuario.id(), usuario.rol(), accion,
                documentoId, resultado, Instant.now(), MDC.get("correlationId"),
                MDC.get("sagaId"), null));
    }

    private SolicitudDocumentoDTO asignarIdentidad(
            SolicitudDocumentoDTO request, UsuarioActual usuario) {
        if ("SERVICE".equals(usuario.rol())) {
            return request;
        }
        String pacienteId = "PACIENTE".equals(usuario.rol())
                ? usuario.id() : request.pacienteId();
        if (request.tipo() == TipoDocumento.FACTURA
                && !"ADMIN".equals(usuario.rol()) && !"SERVICE".equals(usuario.rol())) {
            throw new SecurityException("Solo ADMIN o SERVICE pueden crear facturas");
        }
        return new SolicitudDocumentoDTO(
                request.tipo(), pacienteId, usuario.id(), request.compartidoCon(),
                request.origenContenido(), request.datos(), request.interpretacionIA(),
                request.metadatosIA(), request.formato(), usuario.tenantId());
    }
}

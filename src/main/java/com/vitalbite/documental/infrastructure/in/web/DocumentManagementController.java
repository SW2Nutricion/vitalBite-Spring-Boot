package com.vitalbite.documental.infrastructure.in.web;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.dto.SolicitudDocumentoDTO;
import com.vitalbite.documental.application.usecase.AnularDocumentoUseCase;
import com.vitalbite.documental.application.usecase.ConsultarDocumentoUseCase;
import com.vitalbite.documental.application.usecase.SolicitarDocumentoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/documentos")
@Tag(name = "Gestión documental", description = "Documentos generados y su ciclo de vida")
public class DocumentManagementController {

    private final SolicitarDocumentoUseCase solicitar;
    private final ConsultarDocumentoUseCase consultar;
    private final AnularDocumentoUseCase anular;

    public DocumentManagementController(
            SolicitarDocumentoUseCase solicitar,
            ConsultarDocumentoUseCase consultar,
            AnularDocumentoUseCase anular) {
        this.solicitar = solicitar;
        this.consultar = consultar;
        this.anular = anular;
    }

    @PostMapping
    @Operation(summary = "Solicitar generación de documento")
    public ResponseEntity<DocumentoResponseDTO> create(
            @RequestBody SolicitudDocumentoDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey) {
        DocumentoResponseDTO response = solicitar.execute(request, idempotencyKey);
        return ResponseEntity.status(201)
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar metadatos y estado del documento")
    public ResponseEntity<DocumentoResponseDTO> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "PACIENTE")
            String role) {
        DocumentoResponseDTO response = consultar.execute(id, userId, role);
        return ResponseEntity.ok()
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular documento como compensación de Saga")
    public ResponseEntity<DocumentoResponseDTO> cancel(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "PACIENTE")
            String role,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey) {
        DocumentoResponseDTO response =
                anular.execute(id, userId, role, idempotencyKey);
        return ResponseEntity.ok()
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }
}

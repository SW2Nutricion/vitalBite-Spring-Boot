package com.vitalbite.documental.infrastructure.in.web;

import com.vitalbite.documental.application.dto.DietPdfRequestDTO;
import com.vitalbite.documental.application.dto.InvoicePdfRequestDTO;
import com.vitalbite.documental.application.usecase.GenerateDietPdfUseCase;
import com.vitalbite.documental.application.usecase.GenerateInvoicePdfUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents/pdf")
@Tag(name = "Documentos", description = "Generación de PDFs")
public class DocumentController {

    private final GenerateDietPdfUseCase generateDietPdf;
    private final GenerateInvoicePdfUseCase generateInvoicePdf;

    public DocumentController(
            GenerateDietPdfUseCase generateDietPdf,
            GenerateInvoicePdfUseCase generateInvoicePdf) {
        this.generateDietPdf = generateDietPdf;
        this.generateInvoicePdf = generateInvoicePdf;
    }

    @PostMapping(value = "/diet", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar PDF de dieta")
    public ResponseEntity<byte[]> generateDietPdf(
            @RequestBody DietPdfRequestDTO request) {
        return pdfResponse("dieta.pdf", generateDietPdf.execute(request));
    }

    @PostMapping(value = "/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar PDF de factura")
    public ResponseEntity<byte[]> generateInvoicePdf(
            @RequestBody InvoicePdfRequestDTO request) {
        return pdfResponse("factura.pdf", generateInvoicePdf.execute(request));
    }

    @PostMapping(value = "/diet/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Vista previa del PDF de dieta")
    public ResponseEntity<byte[]> previewDietPdf(
            @RequestBody DietPdfRequestDTO request) {
        return pdfResponse("preview-dieta.pdf", generateDietPdf.execute(request));
    }

    private ResponseEntity<byte[]> pdfResponse(String filename, byte[] content) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=" + filename)
                .body(content);
    }
}

package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.InvoicePdfRequestDTO;
import com.vitalbite.documental.application.port.out.PdfGeneratorPort;

public final class GenerateInvoicePdfUseCase {

    private final PdfGeneratorPort pdfGenerator;

    public GenerateInvoicePdfUseCase(PdfGeneratorPort pdfGenerator) {
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] execute(InvoicePdfRequestDTO request) {
        return pdfGenerator.generateInvoicePdf(request);
    }
}

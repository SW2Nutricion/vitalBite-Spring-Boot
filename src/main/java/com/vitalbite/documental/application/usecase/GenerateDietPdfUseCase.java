package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DietPdfRequestDTO;
import com.vitalbite.documental.application.port.out.PdfGeneratorPort;

public final class GenerateDietPdfUseCase {

    private final PdfGeneratorPort pdfGenerator;

    public GenerateDietPdfUseCase(PdfGeneratorPort pdfGenerator) {
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] execute(DietPdfRequestDTO request) {
        return pdfGenerator.generateDietPdf(request);
    }
}

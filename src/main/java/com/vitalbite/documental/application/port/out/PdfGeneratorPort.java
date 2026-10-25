package com.vitalbite.documental.application.port.out;

import com.vitalbite.documental.application.dto.DietPdfRequestDTO;
import com.vitalbite.documental.application.dto.InvoicePdfRequestDTO;

public interface PdfGeneratorPort {

    byte[] generateDietPdf(DietPdfRequestDTO request);

    byte[] generateInvoicePdf(InvoicePdfRequestDTO request);
}

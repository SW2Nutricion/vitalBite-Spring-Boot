package com.vitalbite.documental.infrastructure.out.generator;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.vitalbite.documental.application.dto.SolicitudDocumentoDTO;
import com.vitalbite.documental.application.port.out.GeneradorDocumentoPort;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SimpleDocumentGeneratorAdapter implements GeneradorDocumentoPort {

    @Override
    public byte[] generar(SolicitudDocumentoDTO request) {
        if ("CSV".equalsIgnoreCase(request.formato())) {
            return csv(request).getBytes(StandardCharsets.UTF_8);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(output));
        Document document = new Document(pdf);
        document.add(new Paragraph("Datos registrados").setBold());
        request.datos().forEach((key, value) ->
                document.add(new Paragraph(key + ": " + value)));
        if (request.interpretacionIA() != null
                && !request.interpretacionIA().isBlank()) {
            document.add(new Paragraph("Interpretación generada por IA").setBold());
            document.add(new Paragraph(
                    "Contenido generado por IA; no sustituye la atención profesional"));
            document.add(new Paragraph(request.interpretacionIA()));
        }
        document.close();
        return output.toByteArray();
    }

    private String csv(SolicitudDocumentoDTO request) {
        StringBuilder result = new StringBuilder("seccion,campo,valor\n");
        for (Map.Entry<String, Object> entry : request.datos().entrySet()) {
            result.append("Datos registrados,")
                    .append(entry.getKey()).append(',')
                    .append(escape(String.valueOf(entry.getValue()))).append('\n');
        }
        if (request.interpretacionIA() != null) {
            result.append("Interpretación generada por IA,contenido,")
                    .append(escape(request.interpretacionIA())).append('\n');
        }
        return result.toString();
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}

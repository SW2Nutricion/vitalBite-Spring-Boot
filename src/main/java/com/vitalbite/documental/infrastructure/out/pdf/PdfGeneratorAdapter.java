package com.vitalbite.documental.infrastructure.out.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.vitalbite.documental.application.dto.DietPdfRequestDTO;
import com.vitalbite.documental.application.dto.InvoicePdfRequestDTO;
import com.vitalbite.documental.application.port.out.PdfGeneratorPort;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PdfGeneratorAdapter implements PdfGeneratorPort {

    private static final DeviceRgb COLOR_PRIMARY =
            new DeviceRgb(34, 139, 87);
    private static final DeviceRgb COLOR_HEADER_MEAL =
            new DeviceRgb(240, 248, 244);
    private static final DeviceRgb COLOR_BORDER =
            new DeviceRgb(180, 220, 195);
    private static final DeviceRgb COLOR_GRAY =
            new DeviceRgb(100, 100, 100);
    private static final DeviceRgb COLOR_LIGHT_GRAY =
            new DeviceRgb(245, 245, 245);
    // Blanco como DeviceRgb para evitar el error de tipos
    private static final DeviceRgb COLOR_WHITE =
            new DeviceRgb(255, 255, 255);

    private static final List<String> ORDEN_COMIDAS =
            Arrays.asList(
                    "Desayuno", "Media mañana",
                    "Almuerzo", "Merienda", "Cena"
            );

    @Override
    public byte[] generateDietPdf(DietPdfRequestDTO request) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4.rotate());

        Document document = new Document(pdfDoc);
        document.setMargins(20, 20, 30, 20);

        agregarEncabezado(document, request);
        agregarTablaSemanal(document, request);
        agregarPiePagina(document, request);

        document.close();
        return outputStream.toByteArray();
    }

    private void agregarEncabezado(Document document,
                                   DietPdfRequestDTO request) {

        String titulo = request.getName() != null
                ? request.getName()
                : "Plan Alimenticio Semanal";

        document.add(new Paragraph(titulo)
                .setFontSize(18)
                .setBold()
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        StringBuilder subtitulo = new StringBuilder();
        if (request.getObjective() != null) {
            subtitulo.append("Objetivo: ")
                    .append(request.getObjective());
        }
        if (request.getEstimatedCalories() != null) {
            subtitulo.append("  |  ")
                    .append(request.getEstimatedCalories())
                    .append(" kcal diarias estimadas");
        }
        if (!subtitulo.isEmpty()) {
            document.add(new Paragraph(subtitulo.toString())
                    .setFontSize(10)
                    .setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));
        }

        String periodo = "";
        if (request.getStartDate() != null
                && request.getEndDate() != null) {
            periodo = "  |   Período: "
                    + request.getStartDate()
                    + " al " + request.getEndDate();
        }

        document.add(new Paragraph(
                "Paciente: " + safe(request.getPatientFullName())
                        + "   |   Nutricionista: "
                        + safe(request.getNutritionistFullName())
                        + periodo)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        if (request.getPhase() != null
                || request.getApproach() != null) {
            StringBuilder extra = new StringBuilder();
            if (request.getPhase() != null) {
                extra.append("Fase: ")
                        .append(request.getPhase());
            }
            if (request.getApproach() != null) {
                if (!extra.isEmpty()) extra.append("   |   ");
                extra.append("Enfoque: ")
                        .append(request.getApproach());
            }
            document.add(new Paragraph(extra.toString())
                    .setFontSize(9)
                    .setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(6));
        }
    }

    private void agregarTablaSemanal(Document document,
                                     DietPdfRequestDTO request) {

        if (request.getDays() == null
                || request.getDays().isEmpty()) {
            document.add(new Paragraph(
                    "No hay días de dieta disponibles.")
                    .setFontColor(ColorConstants.RED));
            return;
        }

        // Ordenar días por dayOrder
        List<DietPdfRequestDTO.DayDTO> diasOrdenados =
                request.getDays().stream()
                        .sorted((a, b) -> {
                            int oa = a.getDayOrder() != null
                                    ? a.getDayOrder() : 0;
                            int ob = b.getDayOrder() != null
                                    ? b.getDayOrder() : 0;
                            return Integer.compare(oa, ob);
                        })
                        .toList();

        int numDias = diasOrdenados.size();

        float[] anchos = new float[numDias + 1];
        anchos[0] = 12f;
        for (int i = 1; i <= numDias; i++) {
            anchos[i] = 100f / numDias;
        }

        Table table = new Table(
                UnitValue.createPercentArray(anchos))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        // Celda vacía esquina superior izquierda
        table.addHeaderCell(new Cell()
                .add(new Paragraph(""))
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(new SolidBorder(COLOR_BORDER, 1)));

        // Encabezados de días
        for (DietPdfRequestDTO.DayDTO day : diasOrdenados) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(safe(day.getDayLabel()))
                            .setFontSize(10)
                            .setBold()
                            .setFontColor(COLOR_WHITE)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(COLOR_PRIMARY)
                    .setBorder(new SolidBorder(COLOR_BORDER, 1))
                    .setPadding(5));
        }

        // Mapa día → comida → items
        Map<String, Map<String, List<DietPdfRequestDTO.ItemDTO>>>
                mapaDias = diasOrdenados.stream()
                .collect(Collectors.toMap(
                        day -> safe(day.getDayLabel()),
                        day -> {
                            if (day.getMeals() == null) {
                                return Map.of();
                            }
                            return day.getMeals().stream()
                                    .collect(Collectors.toMap(
                                            meal -> safe(meal.getName()),
                                            meal -> meal.getItems() != null
                                                    ? meal.getItems()
                                                    : List.of(),
                                            (a, b) -> a
                                    ));
                        }
                ));

        // Filas por tipo de comida
        boolean filaAlterna = false;

        for (String tipoComida : ORDEN_COMIDAS) {

            // Aquí está la corrección del tipo incompatible
            // usamos DeviceRgb en ambos casos
            DeviceRgb colorFila = filaAlterna
                    ? COLOR_LIGHT_GRAY
                    : COLOR_WHITE;

            // Celda etiqueta tipo de comida
            table.addCell(new Cell()
                    .add(new Paragraph(tipoComida)
                            .setFontSize(9)
                            .setBold()
                            .setFontColor(COLOR_PRIMARY)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(COLOR_HEADER_MEAL)
                    .setBorder(new SolidBorder(COLOR_BORDER, 1))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(5));

            // Celdas de alimentos por día
            for (DietPdfRequestDTO.DayDTO day :
                    diasOrdenados) {

                Map<String, List<DietPdfRequestDTO.ItemDTO>>
                        comidasDia = mapaDias.getOrDefault(
                        safe(day.getDayLabel()), Map.of());

                List<DietPdfRequestDTO.ItemDTO> items =
                        comidasDia.getOrDefault(
                                tipoComida, List.of());

                StringBuilder sb = new StringBuilder();
                for (DietPdfRequestDTO.ItemDTO item : items) {
                    sb.append("• ").append(safe(item.getName()));
                    if (item.getPortion() != null) {
                        sb.append(" ").append(item.getPortion());
                    }
                    if (item.getCalories() != null) {
                        sb.append(" (")
                                .append(item.getCalories())
                                .append(" kcal)");
                    }
                    sb.append("\n");
                }

                String contenido = !sb.isEmpty()
                        ? sb.toString().trim()
                        : "—";

                table.addCell(new Cell()
                        .add(new Paragraph(contenido)
                                .setFontSize(8)
                                .setTextAlignment(TextAlignment.LEFT))
                        .setBackgroundColor(colorFila)
                        .setBorder(new SolidBorder(COLOR_BORDER, 1))
                        .setPadding(4));
            }

            filaAlterna = !filaAlterna;
        }

        document.add(table);
    }

    private void agregarPiePagina(Document document,
                                  DietPdfRequestDTO request) {

        StringBuilder pie = new StringBuilder(
                "Generado por VitalBite");

        if (request.getNutritionistFullName() != null) {
            pie.append("  |  ")
                    .append(request.getNutritionistFullName());
        }

        if (request.getAdherencePercent() != null) {
            pie.append("  |  Adherencia actual: ")
                    .append(String.format("%.0f",
                            request.getAdherencePercent()))
                    .append("%");
        }

        pie.append("  |  Este plan es personal y "
                + "no debe compartirse sin autorización.");

        document.add(new Paragraph(pie.toString())
                .setFontSize(8)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }

    @Override
    public byte[] generateInvoicePdf(InvoicePdfRequestDTO request) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setMargins(40, 40, 40, 40);

        // Header
        document.add(new Paragraph("FACTURA DE SUSCRIPCIÓN")
                .setFontSize(22)
                .setBold()
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20));

        // Details
        document.add(new Paragraph("Clínica / Usuario: " + safe(request.getClinicName()))
                .setFontSize(12).setBold());
        document.add(new Paragraph("Fecha de facturación: " + safe(request.getDate()))
                .setFontSize(10).setFontColor(COLOR_GRAY).setMarginBottom(20));

        // Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 20, 20}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(30);

        table.addHeaderCell(new Cell().add(new Paragraph("Descripción")).setBackgroundColor(COLOR_PRIMARY).setFontColor(COLOR_WHITE));
        table.addHeaderCell(new Cell().add(new Paragraph("Periodo")).setBackgroundColor(COLOR_PRIMARY).setFontColor(COLOR_WHITE));
        table.addHeaderCell(new Cell().add(new Paragraph("Subtotal")).setBackgroundColor(COLOR_PRIMARY).setFontColor(COLOR_WHITE));

        table.addCell(new Cell().add(new Paragraph("Suscripción Plan: " + safe(request.getPlanName()))).setPadding(10));
        table.addCell(new Cell().add(new Paragraph(safe(request.getBillingPeriod()))).setPadding(10));
        table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", request.getAmount()))).setPadding(10));

        document.add(table);

        // Total
        document.add(new Paragraph("TOTAL: $" + String.format("%.2f", request.getAmount()))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(40));

        // Footer / Blockchain Hash
        document.add(new Paragraph("Registro de Auditoría Blockchain")
                .setFontSize(10)
                .setBold()
                .setFontColor(COLOR_PRIMARY)
                .setMarginBottom(5));
        
        document.add(new Paragraph("ID Transacción (Hash): " + safe(request.getTransactionHash()))
                .setFontSize(8)
                .setFontColor(COLOR_GRAY));

        document.close();
        return outputStream.toByteArray();
    }
}
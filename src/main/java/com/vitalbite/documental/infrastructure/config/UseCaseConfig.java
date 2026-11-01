package com.vitalbite.documental.infrastructure.config;

import com.vitalbite.documental.application.port.out.PdfGeneratorPort;
import com.vitalbite.documental.application.usecase.GenerateDietPdfUseCase;
import com.vitalbite.documental.application.usecase.GenerateInvoicePdfUseCase;
import com.vitalbite.documental.application.usecase.AnularDocumentoUseCase;
import com.vitalbite.documental.application.usecase.ConsultarDocumentoUseCase;
import com.vitalbite.documental.application.usecase.SolicitarDocumentoUseCase;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.application.port.out.GeneradorDocumentoPort;
import com.vitalbite.documental.infrastructure.out.pdf.PdfGeneratorAdapter;
import com.vitalbite.documental.infrastructure.out.generator.SimpleDocumentGeneratorAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public PdfGeneratorPort pdfGeneratorPort() {
        return new PdfGeneratorAdapter();
    }

    @Bean
    public GenerateDietPdfUseCase generateDietPdfUseCase(
            PdfGeneratorPort pdfGeneratorPort) {
        return new GenerateDietPdfUseCase(pdfGeneratorPort);
    }

    @Bean
    public GenerateInvoicePdfUseCase generateInvoicePdfUseCase(
            PdfGeneratorPort pdfGeneratorPort) {
        return new GenerateInvoicePdfUseCase(pdfGeneratorPort);
    }

    @Bean
    public GeneradorDocumentoPort generadorDocumentoPort() {
        return new SimpleDocumentGeneratorAdapter();
    }

    @Bean
    public SolicitarDocumentoUseCase solicitarDocumentoUseCase(
            DocumentoRepositoryPort repository,
            GeneradorDocumentoPort generator) {
        return new SolicitarDocumentoUseCase(repository, generator, 10_000_000);
    }

    @Bean
    public ConsultarDocumentoUseCase consultarDocumentoUseCase(
            DocumentoRepositoryPort repository) {
        return new ConsultarDocumentoUseCase(repository);
    }

    @Bean
    public AnularDocumentoUseCase anularDocumentoUseCase(
            DocumentoRepositoryPort repository) {
        return new AnularDocumentoUseCase(repository);
    }
}

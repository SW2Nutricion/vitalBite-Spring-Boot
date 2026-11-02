package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.port.out.AlmacenArchivoPort;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.application.port.out.UsuarioActualPort.UsuarioActual;
import com.vitalbite.documental.domain.model.Documento;
import com.vitalbite.documental.domain.model.EstadoDocumento;
import com.vitalbite.documental.application.exception.DocumentoNoEncontrado;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public final class DescargarDocumentoUseCase {

    private final DocumentoRepositoryPort repository;
    private final AlmacenArchivoPort almacen;

    public DescargarDocumentoUseCase(DocumentoRepositoryPort repository,
                                     AlmacenArchivoPort almacen) {
        this.repository = repository;
        this.almacen = almacen;
    }

    public ArchivoDocumento execute(UUID id, UsuarioActual usuario) {
        Documento documento = repository.findById(id)
                .orElseThrow(DocumentoNoEncontrado::new);
        if (!documento.puedeConsultar(usuario.id(), usuario.rol(), usuario.tenantId())) {
            throw new SecurityException("El usuario no puede descargar este documento");
        }
        if (documento.estado() == EstadoDocumento.ANULADO) {
            throw new IllegalStateException("El documento está anulado");
        }
        if (documento.estado() == EstadoDocumento.FALLIDO
                || documento.estado() == EstadoDocumento.PENDIENTE) {
            throw new IllegalStateException("El documento todavía no está disponible");
        }
        byte[] contenido = almacen.leer(id)
                .orElseThrow(() -> new IllegalStateException("Contenido del documento no encontrado"));
        String hash = Base64.getEncoder().encodeToString(sha256(contenido));
        if (!hash.equals(documento.hashSha256())) {
            throw new IllegalStateException("La verificación de integridad del documento falló");
        }
        return new ArchivoDocumento(contenido, contentType(documento.formato()),
                documento.formato().toLowerCase() + "-" + id + "." + extension(documento.formato()),
                hash);
    }

    private byte[] sha256(byte[] contenido) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(contenido);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo verificar el hash del documento", exception);
        }
    }

    private String contentType(String formato) {
        if ("PDF".equalsIgnoreCase(formato)) {
            return "application/pdf";
        }
        if ("CSV".equalsIgnoreCase(formato)) {
            return "text/csv; charset=UTF-8";
        }
        return "application/octet-stream";
    }

    private String extension(String formato) {
        return "PDF".equalsIgnoreCase(formato) ? "pdf"
                : "CSV".equalsIgnoreCase(formato) ? "csv" : "bin";
    }

    public record ArchivoDocumento(byte[] contenido, String contentType,
                                   String filename, String etag) {
        public ArchivoDocumento {
            contenido = contenido.clone();
        }
    }
}

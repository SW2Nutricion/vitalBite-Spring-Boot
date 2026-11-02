package com.vitalbite.documental.application.exception;

public class DocumentoNoEncontrado extends RuntimeException {
    public DocumentoNoEncontrado() {
        super("Documento no encontrado");
    }
}

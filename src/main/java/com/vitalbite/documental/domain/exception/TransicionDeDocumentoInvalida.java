package com.vitalbite.documental.domain.exception;

public class TransicionDeDocumentoInvalida extends IllegalStateException {

    public TransicionDeDocumentoInvalida(String mensaje) {
        super(mensaje);
    }
}

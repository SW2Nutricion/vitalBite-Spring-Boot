package com.vitalbite.documental.application.exception;

public class ArchivoExcedeLimiteException extends RuntimeException {

    public ArchivoExcedeLimiteException(String mensaje) {
        super(mensaje);
    }
}

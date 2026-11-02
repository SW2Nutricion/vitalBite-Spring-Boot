package com.vitalbite.documental.application.port.out;

public interface UsuarioActualPort {

    UsuarioActual actual();

    record UsuarioActual(String id, String rol, String tenantId) {
    }
}

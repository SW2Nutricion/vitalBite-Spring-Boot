package com.vitalbite.documental;

import com.vitalbite.documental.infrastructure.out.persistence.InMemoryAlmacenArchivoAdapter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlmacenArchivoTest {

    @Test
    void guardarYLeerConservaLosMismosBytes() {
        InMemoryAlmacenArchivoAdapter almacen = new InMemoryAlmacenArchivoAdapter();
        UUID documentoId = UUID.randomUUID();
        byte[] esperado = new byte[]{0, 1, -2, 127};

        almacen.guardar(documentoId, esperado);

        assertArrayEquals(esperado, almacen.leer(documentoId).orElseThrow());
        assertTrue(almacen.leer(UUID.randomUUID()).isEmpty());
    }
}

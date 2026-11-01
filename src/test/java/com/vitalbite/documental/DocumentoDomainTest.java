package com.vitalbite.documental;

import com.vitalbite.documental.domain.exception.TransicionDeDocumentoInvalida;
import com.vitalbite.documental.domain.model.AclDocumento;
import com.vitalbite.documental.domain.model.Documento;
import com.vitalbite.documental.domain.model.EstadoDocumento;
import com.vitalbite.documental.domain.model.OrigenContenido;
import com.vitalbite.documental.domain.model.TipoDocumento;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoDomainTest {

    @Test
    void patientOnlySeesDocumentsAllowedByAcl() {
        Documento document = document();

        assertTrue(document.puedeConsultar("patient-1", "PACIENTE"));
        assertFalse(document.puedeConsultar("patient-2", "PACIENTE"));
        assertTrue(document.puedeConsultar("admin", "ADMIN"));
    }

    @Test
    void generatedDocumentCanOnlyBeCancelled() {
        Documento document = document();
        document.marcarGenerado(10, "hash");
        document.anular();

        assertEquals(EstadoDocumento.ANULADO, document.estado());
        assertDoesNotThrow(document::anular);
    }

    @Test
    void finalDocumentCannotBeGeneratedAgain() {
        Documento document = document();
        document.marcarFallido();

        assertThrows(TransicionDeDocumentoInvalida.class,
                () -> document.marcarGenerado(10, "hash"));
    }

    private Documento document() {
        return new Documento(
                null,
                TipoDocumento.REPORTE_EVOLUCION,
                "patient-1",
                new AclDocumento("nutritionist-1", "patient-1", Set.of()),
                OrigenContenido.REGISTRADO,
                null,
                "PDF");
    }
}

package com.vitalbite.documental.application.port.out;

import com.vitalbite.documental.application.dto.SolicitudDocumentoDTO;

public interface GeneradorDocumentoPort {

    byte[] generar(SolicitudDocumentoDTO solicitud);
}

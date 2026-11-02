package com.vitalbite.documental.infrastructure.in.web;

import com.vitalbite.documental.application.port.out.AuditoriaPort;
import com.vitalbite.documental.application.port.out.UsuarioActualPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {
    private final AuditoriaPort auditoria;
    private final UsuarioActualPort usuarioActual;

    public AuditoriaController(AuditoriaPort auditoria, UsuarioActualPort usuarioActual) {
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    public ResponseEntity<AuditoriaPort.Page> buscar(
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) String sub,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!"ADMIN".equals(usuarioActual.actual().rol())) {
            throw new SecurityException("Solo ADMIN puede consultar la auditoría");
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page debe ser >= 0 y size debe estar entre 1 y 100");
        }
        return ResponseEntity.ok(auditoria.buscar(accion, resultado, sub, page, size));
    }
}

package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR_QA')")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportar(
            @RequestParam String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String estado) {
        if (!"pdf".equalsIgnoreCase(formato)) {
            throw new IllegalArgumentException("Formato no soportado: " + formato);
        }

        byte[] contenido = reporteService.generarReportePdf(desde, hasta, responsableId, modulo, prioridad, estado);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", reporteService.generarNombreArchivo(desde, hasta));

        return ResponseEntity.ok()
                .headers(headers)
                .body(contenido);
    }
}

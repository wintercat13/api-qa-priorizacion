package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse;
import com.qapriorizacion.api.service.TrazabilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trazabilidad")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QA_TESTER', 'ADMINISTRADOR_QA', 'DESARROLLADOR')")
public class TrazabilidadController {

    private final TrazabilidadService trazabilidadService;

    @GetMapping("/{casoPruebaId}")
    public ResponseEntity<ApiResponse<TrazabilidadResponse>> obtener(@PathVariable Long casoPruebaId) {
        TrazabilidadResponse trazabilidad = trazabilidadService.obtenerPorCasoPrueba(casoPruebaId);
        return ResponseEntity.ok(ApiResponse.success(trazabilidad));
    }
}

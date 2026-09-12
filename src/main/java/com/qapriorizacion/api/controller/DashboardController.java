package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.TendenciaResponse;
import com.qapriorizacion.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QA_TESTER', 'ADMINISTRADOR_QA', 'DESARROLLADOR')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> obtenerDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long responsableId) {
        DashboardResponse dashboard = dashboardService.obtenerDashboard(desde, hasta, responsableId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/tendencias")
    public ResponseEntity<ApiResponse<List<TendenciaResponse>>> obtenerTendencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false, defaultValue = "dia") String agrupacion) {
        List<TendenciaResponse> tendencias = dashboardService.obtenerTendencias(desde, hasta, responsableId, agrupacion);
        return ResponseEntity.ok(ApiResponse.success(tendencias));
    }
}

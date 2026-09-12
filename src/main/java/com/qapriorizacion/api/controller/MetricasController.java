package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.service.MetricasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/metricas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QA_TESTER', 'ADMINISTRADOR_QA')")
public class MetricasController {

    private final MetricasService metricasService;

    @GetMapping
    public ResponseEntity<ApiResponse<MetricasResponse>> obtenerMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        MetricasResponse metricas = metricasService.calcularMetricas(desde, hasta);
        return ResponseEntity.ok(ApiResponse.success(metricas));
    }
}

package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.request.ConfiguracionCriteriosRequest;
import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.ConfiguracionCriteriosResponse;
import com.qapriorizacion.api.dto.response.CriterioPriorizacionResponse;
import com.qapriorizacion.api.service.CriterioPriorizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/criterios-priorizacion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR_QA')")
public class CriterioPriorizacionController {

    private final CriterioPriorizacionService criterioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CriterioPriorizacionResponse>>> listar() {
        List<CriterioPriorizacionResponse> criterios = criterioService.listar();
        return ResponseEntity.ok(ApiResponse.success(criterios));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ConfiguracionCriteriosResponse>> actualizar(
            @Valid @RequestBody ConfiguracionCriteriosRequest request) {
        ConfiguracionCriteriosResponse resultado = criterioService.actualizar(request);
        return ResponseEntity.ok(ApiResponse.success(resultado));
    }
}

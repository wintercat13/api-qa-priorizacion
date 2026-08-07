package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.request.CasoPruebaUpdateRequest;
import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.service.CasoPruebaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/casos-prueba")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QA_TESTER', 'ADMINISTRADOR_QA')")
public class CasoPruebaController {

    private final CasoPruebaService casoPruebaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CasoPruebaResponse>> crear(
            @Valid @RequestBody CasoPruebaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CasoPruebaResponse creado = casoPruebaService.crear(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(creado));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CasoPruebaResponse>>> listar() {
        List<CasoPruebaResponse> casos = casoPruebaService.listar();
        return ResponseEntity.ok(ApiResponse.success(casos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CasoPruebaResponse>> obtener(@PathVariable Long id) {
        CasoPruebaResponse caso = casoPruebaService.obtener(id);
        return ResponseEntity.ok(ApiResponse.success(caso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CasoPruebaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CasoPruebaUpdateRequest request) {
        CasoPruebaResponse actualizado = casoPruebaService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(actualizado));
    }
}

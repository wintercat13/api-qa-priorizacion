package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

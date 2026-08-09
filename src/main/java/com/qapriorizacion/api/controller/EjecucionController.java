package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.request.EjecucionRequest;
import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.EjecucionResponse;
import com.qapriorizacion.api.service.EjecucionService;
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
@RequestMapping("/api/v1/ejecuciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QA_TESTER', 'ADMINISTRADOR_QA')")
public class EjecucionController {

    private final EjecucionService ejecucionService;

    @PostMapping
    public ResponseEntity<ApiResponse<EjecucionResponse>> registrar(
            @Valid @RequestBody EjecucionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        EjecucionResponse registrada = ejecucionService.registrar(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(registrada));
    }
}

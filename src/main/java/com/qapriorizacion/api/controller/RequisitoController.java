package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.RequisitoResponse;
import com.qapriorizacion.api.service.RequisitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requisitos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QA_TESTER', 'ADMINISTRADOR_QA')")
public class RequisitoController {

    private final RequisitoService requisitoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RequisitoResponse>>> listar() {
        List<RequisitoResponse> requisitos = requisitoService.listar();
        return ResponseEntity.ok(ApiResponse.success(requisitos));
    }
}

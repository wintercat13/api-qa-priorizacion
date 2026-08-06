package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.request.UsuarioRequest;
import com.qapriorizacion.api.dto.response.ApiResponse;
import com.qapriorizacion.api.dto.response.UsuarioResponse;
import com.qapriorizacion.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR_QA')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse actualizado = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(actualizado));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<UsuarioResponse>> desactivar(@PathVariable Long id) {
        UsuarioResponse desactivado = usuarioService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success(desactivado));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listar() {
        List<UsuarioResponse> usuarios = usuarioService.listar();
        return ResponseEntity.ok(ApiResponse.success(usuarios));
    }
}

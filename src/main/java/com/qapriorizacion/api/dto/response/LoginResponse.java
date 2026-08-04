package com.qapriorizacion.api.dto.response;

public record LoginResponse(String token, UsuarioResumenResponse usuario) {
}

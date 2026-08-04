package com.qapriorizacion.api.dto.response;

import com.qapriorizacion.api.entity.enums.RolUsuario;

public record UsuarioResumenResponse(Long id, String nombre, RolUsuario rol) {
}

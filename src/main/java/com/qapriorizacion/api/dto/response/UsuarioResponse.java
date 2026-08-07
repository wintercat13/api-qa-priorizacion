package com.qapriorizacion.api.dto.response;

import com.qapriorizacion.api.entity.enums.RolUsuario;

public record UsuarioResponse(Long id, String nombre, String correo, RolUsuario rol, boolean activo) {
}

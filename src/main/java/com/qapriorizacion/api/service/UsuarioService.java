package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.UsuarioRequest;
import com.qapriorizacion.api.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse actualizar(Long id, UsuarioRequest request);

    UsuarioResponse desactivar(Long id);

    List<UsuarioResponse> listar();
}

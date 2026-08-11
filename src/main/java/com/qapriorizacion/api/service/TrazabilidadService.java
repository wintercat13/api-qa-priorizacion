package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.response.TrazabilidadResponse;

public interface TrazabilidadService {

    TrazabilidadResponse obtenerPorCasoPrueba(Long casoPruebaId);
}

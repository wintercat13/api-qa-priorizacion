package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.EjecucionRequest;
import com.qapriorizacion.api.dto.response.EjecucionResponse;

public interface EjecucionService {

    EjecucionResponse registrar(EjecucionRequest request, String correoEjecutor);
}

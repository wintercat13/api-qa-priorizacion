package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.ConfiguracionCriteriosRequest;
import com.qapriorizacion.api.dto.response.ConfiguracionCriteriosResponse;
import com.qapriorizacion.api.dto.response.CriterioPriorizacionResponse;

import java.util.List;

public interface CriterioPriorizacionService {

    List<CriterioPriorizacionResponse> listar();

    ConfiguracionCriteriosResponse actualizar(ConfiguracionCriteriosRequest request);
}

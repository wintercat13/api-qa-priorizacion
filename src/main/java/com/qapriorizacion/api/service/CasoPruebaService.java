package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;

public interface CasoPruebaService {

    CasoPruebaResponse crear(CasoPruebaRequest request, String correoResponsable);
}

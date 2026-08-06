package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;

import java.util.List;

public interface CasoPruebaService {

    CasoPruebaResponse crear(CasoPruebaRequest request, String correoResponsable);

    List<CasoPruebaResponse> listar();
}

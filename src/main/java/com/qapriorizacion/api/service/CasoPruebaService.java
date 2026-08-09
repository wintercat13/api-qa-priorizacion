package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.request.CasoPruebaUpdateRequest;
import com.qapriorizacion.api.dto.request.VerificarDuplicidadRequest;
import com.qapriorizacion.api.dto.response.CasoObsoletoResponse;
import com.qapriorizacion.api.dto.response.CasoPriorizadoResponse;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.dto.response.DuplicidadResponse;

import java.util.List;

public interface CasoPruebaService {

    CasoPruebaResponse crear(CasoPruebaRequest request, String correoResponsable);

    List<CasoPruebaResponse> listar();

    CasoPruebaResponse actualizar(Long id, CasoPruebaUpdateRequest request);

    CasoPruebaResponse obtener(Long id);

    DuplicidadResponse verificarDuplicidad(VerificarDuplicidadRequest request);

    CasoPruebaResponse confirmarNoDuplicado(Long id);

    List<CasoObsoletoResponse> listarObsoletos();

    CasoPruebaResponse archivar(Long id);

    int ejecutarRevisionObsolescencia();

    List<CasoPriorizadoResponse> listarColaPriorizada(String modulo);

    int recalcularScores();
}

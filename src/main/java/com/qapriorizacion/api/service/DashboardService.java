package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.TendenciaResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardResponse obtenerDashboard(LocalDate desde, LocalDate hasta, Long responsableId);

    List<TendenciaResponse> obtenerTendencias(LocalDate desde, LocalDate hasta, Long responsableId, String agrupacion);
}

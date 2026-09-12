package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.response.MetricasResponse;

import java.time.LocalDate;

public interface MetricasService {

    MetricasResponse calcularMetricas(LocalDate desde, LocalDate hasta);
}

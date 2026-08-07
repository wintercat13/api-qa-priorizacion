package com.qapriorizacion.api.service;

import com.qapriorizacion.api.entity.enums.Criticidad;

import java.math.BigDecimal;

public interface PrioridadService {

    BigDecimal calcularScore(Criticidad criticidad, int contadorFallos);
}

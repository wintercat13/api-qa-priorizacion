package com.qapriorizacion.api.service;

import com.qapriorizacion.api.entity.CriterioPriorizacion;
import com.qapriorizacion.api.entity.enums.Criticidad;

import java.math.BigDecimal;
import java.util.List;

public interface PrioridadService {

    BigDecimal calcularScore(Criticidad criticidad, int contadorFallos);

    BigDecimal calcularScore(Criticidad criticidad, int contadorFallos, List<CriterioPriorizacion> criterios);
}

package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.service.PrioridadService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PrioridadServiceImpl implements PrioridadService {

    @Override
    public BigDecimal calcularScore(Criticidad criticidad, int contadorFallos) {
        BigDecimal base = switch (criticidad) {
            case ALTA -> BigDecimal.valueOf(9.0);
            case MEDIA -> BigDecimal.valueOf(6.0);
            case BAJA -> BigDecimal.valueOf(3.0);
        };

        BigDecimal incremento = BigDecimal.valueOf(contadorFallos * 0.5);
        return base.add(incremento).min(BigDecimal.TEN).setScale(2, RoundingMode.HALF_UP);
    }
}

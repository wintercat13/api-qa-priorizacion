package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.entity.CriterioPriorizacion;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.service.PrioridadService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PrioridadServiceImpl implements PrioridadService {

    @Override
    public BigDecimal calcularScore(Criticidad criticidad, int contadorFallos) {
        return calcularScore(criticidad, contadorFallos, List.of());
    }

    @Override
    public BigDecimal calcularScore(Criticidad criticidad, int contadorFallos, List<CriterioPriorizacion> criterios) {
        if (criterios == null || criterios.isEmpty()) {
            return calcularScoreBase(criticidad, contadorFallos);
        }

        BigDecimal scoreCriticidad = BigDecimal.valueOf(switch (criticidad) {
            case ALTA -> 9.0;
            case MEDIA -> 6.0;
            case BAJA -> 3.0;
        });

        BigDecimal scoreFallos = BigDecimal.valueOf(Math.min(contadorFallos * 2.0, 10.0));

        BigDecimal scoreRiesgo = scoreCriticidad;
        BigDecimal frecuenciaUso = BigDecimal.valueOf(7.0);

        BigDecimal ponderado = BigDecimal.ZERO;
        for (CriterioPriorizacion c : criterios) {
            if (!c.isActivo()) {
                continue;
            }
            BigDecimal peso = c.getPeso();
            BigDecimal valor = switch (normalizarNombre(c.getNombre())) {
                case "criticidad" -> scoreCriticidad;
                case "riesgo" -> scoreRiesgo;
                case "fallos" -> scoreFallos;
                case "frecuencia" -> frecuenciaUso;
                default -> scoreCriticidad;
            };
            ponderado = ponderado.add(valor.multiply(peso));
        }

        return ponderado.setScale(2, RoundingMode.HALF_UP).min(BigDecimal.TEN);
    }

    private BigDecimal calcularScoreBase(Criticidad criticidad, int contadorFallos) {
        BigDecimal base = switch (criticidad) {
            case ALTA -> BigDecimal.valueOf(9.0);
            case MEDIA -> BigDecimal.valueOf(6.0);
            case BAJA -> BigDecimal.valueOf(3.0);
        };

        BigDecimal incremento = BigDecimal.valueOf(contadorFallos * 0.5);
        return base.add(incremento).min(BigDecimal.TEN).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizarNombre(String nombre) {
        String lower = nombre.toLowerCase();
        if (lower.contains("criticidad")) {
            return "criticidad";
        }
        if (lower.contains("riesgo") || lower.contains("impacto") || lower.contains("financiero")) {
            return "riesgo";
        }
        if (lower.contains("fallo") || lower.contains("historial")) {
            return "fallos";
        }
        if (lower.contains("frecuencia") || lower.contains("uso")) {
            return "frecuencia";
        }
        return "criticidad";
    }
}

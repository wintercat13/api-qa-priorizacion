package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.entity.CriterioPriorizacion;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.service.PrioridadService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrioridadServiceImplTest {

    private final PrioridadService prioridadService = new PrioridadServiceImpl();

    @Test
    void calcularScore_deberiaRetornarValorBaseSegunCriticidad() {
        assertThat(prioridadService.calcularScore(Criticidad.ALTA, 0)).isEqualByComparingTo(BigDecimal.valueOf(9.0));
        assertThat(prioridadService.calcularScore(Criticidad.MEDIA, 0)).isEqualByComparingTo(BigDecimal.valueOf(6.0));
        assertThat(prioridadService.calcularScore(Criticidad.BAJA, 0)).isEqualByComparingTo(BigDecimal.valueOf(3.0));
    }

    @Test
    void calcularScore_deberiaIncrementarPorFallos() {
        assertThat(prioridadService.calcularScore(Criticidad.MEDIA, 2))
                .isEqualByComparingTo(BigDecimal.valueOf(7.0));
    }

    @Test
    void calcularScore_noDeberiaExceder10() {
        assertThat(prioridadService.calcularScore(Criticidad.ALTA, 10))
                .isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void calcularScoreConCriterios_deberiaAplicarPesosCorrectamente() {
        List<CriterioPriorizacion> criterios = List.of(
                CriterioPriorizacion.builder().nombre("Criticidad del proceso").peso(new BigDecimal("0.35")).activo(true).build(),
                CriterioPriorizacion.builder().nombre("Riesgo/impacto financiero").peso(new BigDecimal("0.30")).activo(true).build(),
                CriterioPriorizacion.builder().nombre("Historial de fallos").peso(new BigDecimal("0.20")).activo(true).build(),
                CriterioPriorizacion.builder().nombre("Frecuencia de uso").peso(new BigDecimal("0.15")).activo(true).build()
        );

        BigDecimal score = prioridadService.calcularScore(Criticidad.ALTA, 1, criterios);

        assertThat(score).isGreaterThan(BigDecimal.ZERO);
        assertThat(score).isLessThanOrEqualTo(BigDecimal.TEN);
    }

    @Test
    void calcularScoreConCriterios_deberiaIgnorarCriteriosInactivos() {
        List<CriterioPriorizacion> criterios = List.of(
                CriterioPriorizacion.builder().nombre("Criticidad del proceso").peso(new BigDecimal("1.00")).activo(true).build(),
                CriterioPriorizacion.builder().nombre("Frecuencia de uso").peso(new BigDecimal("0.50")).activo(false).build()
        );

        BigDecimal score = prioridadService.calcularScore(Criticidad.MEDIA, 0, criterios);

        assertThat(score).isEqualByComparingTo(new BigDecimal("6.00"));
    }
}

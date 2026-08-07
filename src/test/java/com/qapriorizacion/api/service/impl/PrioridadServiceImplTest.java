package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.service.PrioridadService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
}

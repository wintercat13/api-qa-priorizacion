package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.service.SimilitudService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SimilitudServiceImplTest {

    private final SimilitudService similitudService = new SimilitudServiceImpl();

    @Test
    void calcularSimilitud_deberiaRetornarUno_cuandoTextosSonIdenticos() {
        BigDecimal resultado = similitudService.calcularSimilitud("Validar transferencia", "Validar transferencia");
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void calcularSimilitud_deberiaRetornarCero_cuandoUnTextoEsVacio() {
        BigDecimal resultado = similitudService.calcularSimilitud("Texto", "");
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calcularSimilitud_deberiaSerMayorQueSetenta_cuandoTextosSonMuySimilares() {
        BigDecimal resultado = similitudService.calcularSimilitud(
                "Validar transferencia entre cuentas del mismo titular",
                "Validar transferencia entre cuentas del mismo titular");
        assertThat(resultado).isGreaterThanOrEqualTo(new BigDecimal("0.70"));
    }

    @Test
    void calcularSimilitud_deberiaSerMenorQueSetenta_cuandoTextosSonDistintos() {
        BigDecimal resultado = similitudService.calcularSimilitud(
                "Validar transferencia entre cuentas",
                "Crear usuario nuevo en plataforma");
        assertThat(resultado).isLessThan(new BigDecimal("0.70"));
    }
}

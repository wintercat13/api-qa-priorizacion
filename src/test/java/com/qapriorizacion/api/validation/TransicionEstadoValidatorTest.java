package com.qapriorizacion.api.validation;

import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransicionEstadoValidatorTest {

    private final TransicionEstadoValidator validator = new TransicionEstadoValidator();

    @Test
    void esValida_deberiaPermitirPendienteAEnCurso() {
        assertThat(validator.esValida(EstadoCasoPrueba.PENDIENTE, EstadoCasoPrueba.EN_CURSO)).isTrue();
    }

    @Test
    void esValida_deberiaPermitirEnCursoAEjecutadoOBloqueado() {
        assertThat(validator.esValida(EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.EJECUTADO)).isTrue();
        assertThat(validator.esValida(EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.BLOQUEADO)).isTrue();
    }

    @Test
    void esValida_deberiaPermitirMismoEstado() {
        assertThat(validator.esValida(EstadoCasoPrueba.PENDIENTE, EstadoCasoPrueba.PENDIENTE)).isTrue();
    }

    @Test
    void esValida_deberiaPermitirEjecutadoAObsoleto() {
        assertThat(validator.esValida(EstadoCasoPrueba.EJECUTADO, EstadoCasoPrueba.OBSOLETO)).isTrue();
    }

    @Test
    void esValida_deberiaRechazarEjecutadoAEnCurso() {
        assertThat(validator.esValida(EstadoCasoPrueba.EJECUTADO, EstadoCasoPrueba.EN_CURSO)).isFalse();
    }
}

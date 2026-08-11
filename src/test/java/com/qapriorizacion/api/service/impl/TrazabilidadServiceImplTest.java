package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.TrazabilidadResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Ejecucion;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrazabilidadServiceImplTest {

    @Mock
    private CasoPruebaRepository casoPruebaRepository;

    @Mock
    private EjecucionRepository ejecucionRepository;

    @InjectMocks
    private TrazabilidadServiceImpl trazabilidadService;

    @Test
    void obtenerPorCasoPrueba_deberiaRetornarTrazabilidadCompleta_cuandoExisteEjecucion() {
        Requisito requisito = Requisito.builder().id(3L).codigo("RF-03").nombre("Transferencias entre cuentas propias").build();
        Usuario ejecutor = Usuario.builder().id(1L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(104L).titulo("Validar transferencia entre cuentas propias")
                .modulo("Transferencias").criticidad(Criticidad.ALTA)
                .estado(EstadoCasoPrueba.BLOQUEADO)
                .requisito(requisito)
                .build();
        Ejecucion ejecucion = Ejecucion.builder()
                .id(501L).casoPrueba(caso).ejecutor(ejecutor)
                .resultado(ResultadoEjecucion.FALLIDO)
                .observaciones("Falla en validación de monto máximo")
                .fechaEjecucion(OffsetDateTime.now())
                .build();

        when(casoPruebaRepository.findById(104L)).thenReturn(Optional.of(caso));
        when(ejecucionRepository.findTopByCasoPruebaIdOrderByFechaEjecucionDesc(104L)).thenReturn(Optional.of(ejecucion));

        TrazabilidadResponse response = trazabilidadService.obtenerPorCasoPrueba(104L);

        assertThat(response.requisito().codigo()).isEqualTo("RF-03");
        assertThat(response.casoPrueba().titulo()).isEqualTo("Validar transferencia entre cuentas propias");
        assertThat(response.ultimaEjecucion().resultado()).isEqualTo(ResultadoEjecucion.FALLIDO);
        assertThat(response.estadoActual()).isEqualTo(EstadoCasoPrueba.BLOQUEADO);
    }

    @Test
    void obtenerPorCasoPrueba_deberiaRetornarUltimaEjecucionNula_cuandoNoHayEjecuciones() {
        Requisito requisito = Requisito.builder().id(3L).codigo("RF-03").nombre("Transferencias entre cuentas propias").build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(104L).titulo("Caso sin ejecuciones")
                .modulo("Transferencias").criticidad(Criticidad.ALTA)
                .estado(EstadoCasoPrueba.PENDIENTE)
                .requisito(requisito)
                .build();

        when(casoPruebaRepository.findById(104L)).thenReturn(Optional.of(caso));
        when(ejecucionRepository.findTopByCasoPruebaIdOrderByFechaEjecucionDesc(104L)).thenReturn(Optional.empty());

        TrazabilidadResponse response = trazabilidadService.obtenerPorCasoPrueba(104L);

        assertThat(response.ultimaEjecucion()).isNull();
        assertThat(response.estadoActual()).isEqualTo(EstadoCasoPrueba.PENDIENTE);
    }

    @Test
    void obtenerPorCasoPrueba_deberiaLanzarRecursoNoEncontrado_cuandoCasoNoExiste() {
        when(casoPruebaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trazabilidadService.obtenerPorCasoPrueba(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Caso de prueba no encontrado");
    }
}

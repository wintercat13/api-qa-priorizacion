package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.ConfiguracionCriteriosRequest;
import com.qapriorizacion.api.dto.request.CriterioPriorizacionRequest;
import com.qapriorizacion.api.dto.response.ConfiguracionCriteriosResponse;
import com.qapriorizacion.api.dto.response.CriterioPriorizacionResponse;
import com.qapriorizacion.api.entity.CriterioPriorizacion;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.SumaPesosInvalidaException;
import com.qapriorizacion.api.repository.CriterioPriorizacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriterioPriorizacionServiceImplTest {

    @Mock
    private CriterioPriorizacionRepository criterioRepository;

    @Mock
    private CasoPruebaServiceImpl casoPruebaService;

    @InjectMocks
    private CriterioPriorizacionServiceImpl criterioService;

    @Test
    void listar_deberiaRetornarTodosLosCriterios() {
        CriterioPriorizacion c1 = CriterioPriorizacion.builder()
                .id(1L).nombre("Criticidad").peso(new BigDecimal("0.35")).activo(true).build();
        CriterioPriorizacion c2 = CriterioPriorizacion.builder()
                .id(2L).nombre("Riesgo").peso(new BigDecimal("0.30")).activo(true).build();

        when(criterioRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CriterioPriorizacionResponse> response = criterioService.listar();

        assertThat(response).hasSize(2);
    }

    @Test
    void actualizar_deberiaGuardarCriteriosYRecalcularScores_cuandoSumaEs100() {
        ConfiguracionCriteriosRequest request = new ConfiguracionCriteriosRequest(List.of(
                new CriterioPriorizacionRequest(1L, "Criticidad", "Desc", new BigDecimal("0.40"), true),
                new CriterioPriorizacionRequest(2L, "Riesgo", "Desc", new BigDecimal("0.60"), true)
        ));
        CriterioPriorizacion c1 = CriterioPriorizacion.builder().id(1L).build();
        CriterioPriorizacion c2 = CriterioPriorizacion.builder().id(2L).build();

        when(criterioRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(criterioRepository.findById(2L)).thenReturn(Optional.of(c2));
        when(criterioRepository.save(any(CriterioPriorizacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(casoPruebaService.recalcularScores()).thenReturn(10);

        ConfiguracionCriteriosResponse response = criterioService.actualizar(request);

        assertThat(response.criteriosActualizados()).isEqualTo(2);
        assertThat(response.casosRecalculados()).isEqualTo(10);
        verify(casoPruebaService).recalcularScores();
    }

    @Test
    void actualizar_deberiaLanzarSumaPesosInvalida_cuandoSumaNoEs100() {
        ConfiguracionCriteriosRequest request = new ConfiguracionCriteriosRequest(List.of(
                new CriterioPriorizacionRequest(1L, "Criticidad", "Desc", new BigDecimal("0.40"), true),
                new CriterioPriorizacionRequest(2L, "Riesgo", "Desc", new BigDecimal("0.30"), true)
        ));

        assertThatThrownBy(() -> criterioService.actualizar(request))
                .isInstanceOf(SumaPesosInvalidaException.class)
                .hasMessageContaining("La suma de los pesos");
    }

    @Test
    void actualizar_deberiaLanzarRecursoNoEncontrado_cuandoCriterioNoExiste() {
        ConfiguracionCriteriosRequest request = new ConfiguracionCriteriosRequest(List.of(
                new CriterioPriorizacionRequest(99L, "Inexistente", "Desc", new BigDecimal("1.00"), true)
        ));

        when(criterioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> criterioService.actualizar(request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Criterio de priorización no encontrado");
    }
}

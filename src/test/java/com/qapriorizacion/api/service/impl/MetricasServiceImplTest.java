package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.service.MetricasService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricasServiceImplTest {

    @Mock
    private CasoPruebaRepository casoPruebaRepository;

    @Mock
    private EjecucionRepository ejecucionRepository;

    @InjectMocks
    private MetricasServiceImpl metricasService;

    @Test
    void calcularMetricas_deberiaRetornarMetricasCalculadas() {
        when(casoPruebaRepository.countByEstadoNot(EstadoCasoPrueba.ARCHIVADO)).thenReturn(100L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.EJECUTADO)).thenReturn(70L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.EN_CURSO)).thenReturn(15L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.BLOQUEADO)).thenReturn(10L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.PENDIENTE)).thenReturn(5L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.ARCHIVADO)).thenReturn(14L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.OBSOLETO)).thenReturn(0L);

        MetricasResponse response = metricasService.calcularMetricas(null, null);

        assertThat(response.cobertura()).isEqualTo(61);
        assertThat(response.porcentajeEjecutado()).isEqualTo(70);
        assertThat(response.casosObsoletosDepurados()).isEqualTo(14);
        assertThat(response.cumplimientoSLA()).isEqualTo(85);
    }

    @Test
    void calcularMetricas_deberiaRetornarCeros_cuandoNoHayDatos() {
        when(casoPruebaRepository.countByEstadoNot(EstadoCasoPrueba.ARCHIVADO)).thenReturn(0L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.EJECUTADO)).thenReturn(0L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.EN_CURSO)).thenReturn(0L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.BLOQUEADO)).thenReturn(0L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.PENDIENTE)).thenReturn(0L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.ARCHIVADO)).thenReturn(0L);
        when(casoPruebaRepository.countByEstado(EstadoCasoPrueba.OBSOLETO)).thenReturn(0L);

        MetricasResponse response = metricasService.calcularMetricas(null, null);

        assertThat(response.cobertura()).isEqualTo(0);
        assertThat(response.porcentajeEjecutado()).isEqualTo(0);
        assertThat(response.casosObsoletosDepurados()).isEqualTo(0);
        assertThat(response.cumplimientoSLA()).isEqualTo(0);
    }
}

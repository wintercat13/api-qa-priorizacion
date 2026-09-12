package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.DashboardKpiResponse;
import com.qapriorizacion.api.dto.response.EstadoEjecucionResponse;
import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.dto.response.SerieResponse;
import com.qapriorizacion.api.dto.response.TendenciaResponse;
import com.qapriorizacion.api.exception.SinDatosReporteException;
import com.qapriorizacion.api.service.CasoPruebaService;
import com.qapriorizacion.api.service.DashboardService;
import com.qapriorizacion.api.service.MetricasService;
import com.qapriorizacion.api.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private MetricasService metricasService;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private CasoPruebaService casoPruebaService;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    void generarReportePdf_deberiaRetornarBytesNoVacios_cuandoHayDatos() {
        when(metricasService.calcularMetricas(any(), any()))
                .thenReturn(new MetricasResponse(78, 71, 14, 92));
        when(dashboardService.obtenerDashboard(any(), any(), any()))
                .thenReturn(new DashboardResponse(
                        new DashboardKpiResponse(10, 5, 50, 1),
                        List.of(new SerieResponse("Módulo A", 7)),
                        List.of(new EstadoEjecucionResponse("Ejecutado", 5, 50),
                                new EstadoEjecucionResponse("Pendiente", 3, 30),
                                new EstadoEjecucionResponse("Bloqueado", 2, 20))));
        when(dashboardService.obtenerTendencias(any(), any(), any(), anyString()))
                .thenReturn(List.of(new TendenciaResponse("2026-W01", 2, 1, 0)));
        when(casoPruebaService.listar()).thenReturn(List.of());

        byte[] pdf = reporteService.generarReportePdf(null, null, null, null, null, null);

        assertThat(pdf).isNotEmpty();
        assertThat(pdf[0]).isEqualTo((byte) 0x25);
        assertThat(pdf[1]).isEqualTo((byte) 0x50);
        assertThat(pdf[2]).isEqualTo((byte) 0x44);
        assertThat(pdf[3]).isEqualTo((byte) 0x46);
    }

    @Test
    void generarReportePdf_deberiaLanzarSinDatosReporte_cuandoNoHayDatos() {
        when(metricasService.calcularMetricas(any(), any()))
                .thenReturn(new MetricasResponse(0, 0, 0, 0));

        assertThatThrownBy(() -> reporteService.generarReportePdf(null, null, null, null, null, null))
                .isInstanceOf(SinDatosReporteException.class)
                .hasMessageContaining("No hay datos suficientes");
    }

    @Test
    void generarNombreArchivo_deberiaIncluirFechas_cuandoSeProveen() {
        assertThat(reporteService.generarNombreArchivo(
                java.time.LocalDate.of(2026, 7, 1),
                java.time.LocalDate.of(2026, 9, 30)))
                .isEqualTo("reporte_qa_2026-07-01_2026-09-30.pdf");
    }
}

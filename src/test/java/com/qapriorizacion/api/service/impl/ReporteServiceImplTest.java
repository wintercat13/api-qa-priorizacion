package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.exception.SinDatosReporteException;
import com.qapriorizacion.api.service.MetricasService;
import com.qapriorizacion.api.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private MetricasService metricasService;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    void generarReportePdf_deberiaRetornarBytesNoVacios_cuandoHayDatos() {
        when(metricasService.calcularMetricas(null, null))
                .thenReturn(new MetricasResponse(78, 71, 14, 92));

        byte[] pdf = reporteService.generarReportePdf(null, null);

        assertThat(pdf).isNotEmpty();
        assertThat(pdf[0]).isEqualTo((byte) 0x25);
        assertThat(pdf[1]).isEqualTo((byte) 0x50);
        assertThat(pdf[2]).isEqualTo((byte) 0x44);
        assertThat(pdf[3]).isEqualTo((byte) 0x46);
    }

    @Test
    void generarReportePdf_deberiaLanzarSinDatosReporte_cuandoNoHayDatos() {
        when(metricasService.calcularMetricas(null, null))
                .thenReturn(new MetricasResponse(0, 0, 0, 0));

        assertThatThrownBy(() -> reporteService.generarReportePdf(null, null))
                .isInstanceOf(SinDatosReporteException.class)
                .hasMessageContaining("No hay datos suficientes");
    }
}

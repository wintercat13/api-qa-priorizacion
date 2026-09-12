package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.EstadoEjecucionResponse;
import com.qapriorizacion.api.dto.response.SerieResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Ejecucion;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private CasoPruebaRepository casoPruebaRepository;

    @Mock
    private EjecucionRepository ejecucionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void obtenerDashboard_deberiaRetornarKpisGraficosYEstados() {
        when(casoPruebaRepository.findAll()).thenReturn(List.of(
                crearCaso(1L, "Transferencias 1", "Transferencias", Criticidad.MEDIA, EstadoCasoPrueba.EJECUTADO, false, 1L),
                crearCaso(2L, "Transferencias 2", "Transferencias", Criticidad.ALTA, EstadoCasoPrueba.PENDIENTE, false, 1L),
                crearCaso(3L, "Autenticación 1", "Autenticación", Criticidad.ALTA, EstadoCasoPrueba.EJECUTADO, true, 2L),
                crearCaso(4L, "Tarjetas 1", "Tarjetas", Criticidad.ALTA, EstadoCasoPrueba.EJECUTADO, false, 1L),
                crearCaso(5L, "Tarjetas 2", "Tarjetas", Criticidad.BAJA, EstadoCasoPrueba.BLOQUEADO, false, 1L),
                crearCaso(6L, "Archivado", "Transferencias", Criticidad.ALTA, EstadoCasoPrueba.ARCHIVADO, false, 1L)
        ));

        DashboardResponse response = dashboardService.obtenerDashboard(null, null, null);

        assertThat(response.kpis().totalCasos()).isEqualTo(5);
        assertThat(response.kpis().prioridadAlta()).isEqualTo(3);
        assertThat(response.kpis().porcentajeEjecutados()).isEqualTo(60);
        assertThat(response.kpis().duplicadosDetectados()).isEqualTo(1);

        assertThat(response.casosPorModulo())
                .containsExactly(
                        new SerieResponse("Autenticación", 1),
                        new SerieResponse("Tarjetas", 2),
                        new SerieResponse("Transferencias", 2));

        assertThat(response.estadoEjecucion())
                .containsExactly(
                        new EstadoEjecucionResponse("Ejecutado", 3, 60),
                        new EstadoEjecucionResponse("Pendiente", 1, 20),
                        new EstadoEjecucionResponse("Bloqueado", 1, 20));
    }

    @Test
    void obtenerDashboard_deberiaRetornarCeros_cuandoNoHayDatos() {
        when(casoPruebaRepository.findAll()).thenReturn(List.of());

        DashboardResponse response = dashboardService.obtenerDashboard(null, null, null);

        assertThat(response.kpis().totalCasos()).isEqualTo(0);
        assertThat(response.kpis().prioridadAlta()).isEqualTo(0);
        assertThat(response.kpis().porcentajeEjecutados()).isEqualTo(0);
        assertThat(response.kpis().duplicadosDetectados()).isEqualTo(0);
        assertThat(response.casosPorModulo()).isEmpty();
        assertThat(response.estadoEjecucion()).containsExactly(
                new EstadoEjecucionResponse("Ejecutado", 0, 0),
                new EstadoEjecucionResponse("Pendiente", 0, 0),
                new EstadoEjecucionResponse("Bloqueado", 0, 0)
        );
    }

    @Test
    void obtenerDashboard_deberiaFiltrarPorResponsable() {
        when(casoPruebaRepository.findAll()).thenReturn(List.of(
                crearCaso(1L, "Caso A", "Módulo A", Criticidad.ALTA, EstadoCasoPrueba.EJECUTADO, false, 1L),
                crearCaso(2L, "Caso B", "Módulo A", Criticidad.ALTA, EstadoCasoPrueba.PENDIENTE, false, 2L)
        ));
        when(usuarioRepository.existsById(2L)).thenReturn(true);

        DashboardResponse response = dashboardService.obtenerDashboard(null, null, 2L);

        assertThat(response.kpis().totalCasos()).isEqualTo(1);
    }

    @Test
    void obtenerTendencias_deberiaAgruparPorDia() {
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        when(ejecucionRepository.findAll()).thenReturn(List.of(
                crearEjecucion(1L, EstadoCasoPrueba.EJECUTADO, hoy.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(), 1L),
                crearEjecucion(2L, EstadoCasoPrueba.PENDIENTE, hoy.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(), 1L),
                crearEjecucion(3L, EstadoCasoPrueba.BLOQUEADO, ayer.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(), 1L)
        ));

        var tendencias = dashboardService.obtenerTendencias(ayer, hoy, null, "dia");

        assertThat(tendencias).hasSize(2);
        assertThat(tendencias.get(0).ejecutados()).isEqualTo(0);
        assertThat(tendencias.get(0).bloqueados()).isEqualTo(1);
        assertThat(tendencias.get(1).ejecutados()).isEqualTo(1);
        assertThat(tendencias.get(1).pendientes()).isEqualTo(1);
    }

    private CasoPrueba crearCaso(Long id, String titulo, String modulo, Criticidad criticidad,
                                 EstadoCasoPrueba estado, boolean posibleDuplicado, Long responsableId) {
        return CasoPrueba.builder()
                .id(id)
                .titulo(titulo)
                .modulo(modulo)
                .criticidad(criticidad)
                .estado(estado)
                .posibleDuplicado(posibleDuplicado)
                .scorePrioridad(BigDecimal.ZERO)
                .contadorFallos(0)
                .responsable(Usuario.builder().id(responsableId).build())
                .requisito(Requisito.builder().id(1L).build())
                .build();
    }

    private Ejecucion crearEjecucion(Long id, EstadoCasoPrueba estado, OffsetDateTime fecha, Long ejecutorId) {
        return Ejecucion.builder()
                .id(id)
                .casoPrueba(CasoPrueba.builder().id(id).estado(estado).build())
                .ejecutor(Usuario.builder().id(ejecutorId).build())
                .fechaEjecucion(fecha)
                .build();
    }
}

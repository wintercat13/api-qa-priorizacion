package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.response.DashboardKpiResponse;
import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.EstadoEjecucionResponse;
import com.qapriorizacion.api.dto.response.SerieResponse;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void obtenerDashboard_deberiaRetornar200ConDatos() throws Exception {
        DashboardResponse response = new DashboardResponse(
                new DashboardKpiResponse(482, 63, 71, 9),
                List.of(
                        new SerieResponse("Autenticación", 120),
                        new SerieResponse("Cartola", 80),
                        new SerieResponse("Perfil", 40),
                        new SerieResponse("Tarjetas", 150),
                        new SerieResponse("Transferencias", 92)
                ),
                List.of(
                        new EstadoEjecucionResponse("Ejecutado", 342, 71),
                        new EstadoEjecucionResponse("Pendiente", 87, 18),
                        new EstadoEjecucionResponse("Bloqueado", 53, 11)
                )
        );

        when(dashboardService.obtenerDashboard(null, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.kpis.totalCasos").value(482))
                .andExpect(jsonPath("$.data.kpis.prioridadAlta").value(63))
                .andExpect(jsonPath("$.data.kpis.porcentajeEjecutados").value(71))
                .andExpect(jsonPath("$.data.kpis.duplicadosDetectados").value(9))
                .andExpect(jsonPath("$.data.casosPorModulo[0].nombre").value("Autenticación"))
                .andExpect(jsonPath("$.data.casosPorModulo[0].valor").value(120))
                .andExpect(jsonPath("$.data.estadoEjecucion[0].estado").value("Ejecutado"))
                .andExpect(jsonPath("$.data.estadoEjecucion[0].cantidad").value(342))
                .andExpect(jsonPath("$.data.estadoEjecucion[0].porcentaje").value(71));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void obtenerDashboard_deberiaFiltrarPorResponsable() throws Exception {
        DashboardResponse response = new DashboardResponse(
                new DashboardKpiResponse(50, 5, 20, 0),
                List.of(new SerieResponse("Transferencias", 50)),
                List.of(new EstadoEjecucionResponse("Ejecutado", 10, 20))
        );

        when(dashboardService.obtenerDashboard(null, null, 2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard")
                        .param("responsableId", "2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpis.totalCasos").value(50));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void obtenerDashboard_deberiaPermitirAccesoAQATester() throws Exception {
        DashboardResponse response = new DashboardResponse(
                new DashboardKpiResponse(10, 2, 30, 1),
                List.of(),
                List.of()
        );

        when(dashboardService.obtenerDashboard(null, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void obtenerTendencias_deberiaRetornar200ConDatos() throws Exception {
        when(dashboardService.obtenerTendencias(null, null, null, "dia")).thenReturn(List.of(
                new com.qapriorizacion.api.dto.response.TendenciaResponse("2026-08-30", 5, 2, 1)
        ));

        mockMvc.perform(get("/api/v1/dashboard/tendencias")
                        .param("agrupacion", "dia")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].periodo").value("2026-08-30"))
                .andExpect(jsonPath("$.data[0].ejecutados").value(5))
                .andExpect(jsonPath("$.data[0].pendientes").value(2))
                .andExpect(jsonPath("$.data[0].bloqueados").value(1));
    }

    @Test
    @WithMockUser(roles = "DESARROLLADOR")
    void obtenerDashboard_deberiaPermitirAccesoADesarrollador() throws Exception {
        DashboardResponse response = new DashboardResponse(
                new DashboardKpiResponse(10, 2, 30, 1),
                List.of(),
                List.of()
        );

        when(dashboardService.obtenerDashboard(null, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.MetricasService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricasController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class MetricasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricasService metricasService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void obtenerMetricas_deberiaRetornar200ConMetricas() throws Exception {
        MetricasResponse response = new MetricasResponse(78, 71, 14, 92);

        when(metricasService.calcularMetricas(null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/metricas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.cobertura").value(78))
                .andExpect(jsonPath("$.data.porcentajeEjecutado").value(71))
                .andExpect(jsonPath("$.data.casosObsoletosDepurados").value(14))
                .andExpect(jsonPath("$.data.cumplimientoSLA").value(92));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void obtenerMetricas_deberiaPermitirAccesoAQATester() throws Exception {
        MetricasResponse response = new MetricasResponse(50, 50, 0, 80);

        when(metricasService.calcularMetricas(null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/metricas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DESARROLLADOR")
    void obtenerMetricas_deberiaRetornar403_cuandoRolEsDesarrollador() throws Exception {
        mockMvc.perform(get("/api/v1/metricas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

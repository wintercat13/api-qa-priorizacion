package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qapriorizacion.api.dto.request.ConfiguracionCriteriosRequest;
import com.qapriorizacion.api.dto.request.CriterioPriorizacionRequest;
import com.qapriorizacion.api.dto.response.ConfiguracionCriteriosResponse;
import com.qapriorizacion.api.dto.response.CriterioPriorizacionResponse;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.CriterioPriorizacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CriterioPriorizacionController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class CriterioPriorizacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @MockitoBean
    private CriterioPriorizacionService criterioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void listar_deberiaRetornar200YCuerpoSuccess() throws Exception {
        List<CriterioPriorizacionResponse> criterios = List.of(
                new CriterioPriorizacionResponse(1L, "Criticidad", "Desc", new BigDecimal("0.35"), true, null),
                new CriterioPriorizacionResponse(2L, "Riesgo", "Desc", new BigDecimal("0.30"), true, null)
        );

        when(criterioService.listar()).thenReturn(criterios);

        mockMvc.perform(get("/api/v1/criterios-priorizacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].peso").value(0.35));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void actualizar_deberiaRetornar200ConResumenDeActualizacion() throws Exception {
        ConfiguracionCriteriosRequest request = new ConfiguracionCriteriosRequest(List.of(
                new CriterioPriorizacionRequest(1L, "Criticidad", "Desc", new BigDecimal("0.40"), true),
                new CriterioPriorizacionRequest(2L, "Riesgo", "Desc", new BigDecimal("0.60"), true)
        ));
        ConfiguracionCriteriosResponse response = new ConfiguracionCriteriosResponse(2, 482);

        when(criterioService.actualizar(any(ConfiguracionCriteriosRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/criterios-priorizacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.criteriosActualizados").value(2))
                .andExpect(jsonPath("$.data.casosRecalculados").value(482));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void actualizar_deberiaRetornar403_cuandoRolNoEsAdministradorQA() throws Exception {
        ConfiguracionCriteriosRequest request = new ConfiguracionCriteriosRequest(List.of(
                new CriterioPriorizacionRequest(1L, "Criticidad", "Desc", new BigDecimal("0.40"), true),
                new CriterioPriorizacionRequest(2L, "Riesgo", "Desc", new BigDecimal("0.60"), true)
        ));

        mockMvc.perform(put("/api/v1/criterios-priorizacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

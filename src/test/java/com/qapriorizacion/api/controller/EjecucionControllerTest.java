package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qapriorizacion.api.dto.request.EjecucionRequest;
import com.qapriorizacion.api.dto.response.EjecucionResponse;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.EjecucionService;
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

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EjecucionController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class EjecucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @MockitoBean
    private EjecucionService ejecucionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "QA_TESTER", username = "tester@banco.cl")
    void registrar_deberiaRetornar201YCuerpoSuccess_cuandoRequestEsValida() throws Exception {
        EjecucionRequest request = new EjecucionRequest(104L, ResultadoEjecucion.FALLIDO, "Falla en validación");
        EjecucionResponse response = new EjecucionResponse(
                501L, 104L, ResultadoEjecucion.FALLIDO, EstadoCasoPrueba.BLOQUEADO, "Falla en validación", OffsetDateTime.now());

        when(ejecucionService.registrar(any(EjecucionRequest.class), eq("tester@banco.cl"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ejecuciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.casoPruebaId").value(104))
                .andExpect(jsonPath("$.data.resultado").value("FALLIDO"))
                .andExpect(jsonPath("$.data.estadoCaso").value("BLOQUEADO"));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void registrar_deberiaRetornar400_cuandoResultadoEsNulo() throws Exception {
        String json = "{\"casoPruebaId\":104,\"observaciones\":\"OK\"}";

        mockMvc.perform(post("/api/v1/ejecuciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DESARROLLADOR")
    void registrar_deberiaRetornar403_cuandoRolNoEsQATesterNiAdministrador() throws Exception {
        EjecucionRequest request = new EjecucionRequest(104L, ResultadoEjecucion.APROBADO, "OK");

        mockMvc.perform(post("/api/v1/ejecuciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

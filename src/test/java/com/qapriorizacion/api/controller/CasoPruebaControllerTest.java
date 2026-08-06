package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.CasoPruebaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CasoPruebaController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class CasoPruebaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @MockitoBean
    private CasoPruebaService casoPruebaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "QA_TESTER", username = "tester@banco.cl")
    void crear_deberiaRetornar201YCuerpoSuccess_cuandoRequestEsValida() throws Exception {
        CasoPruebaRequest request = new CasoPruebaRequest(
                "Validar transferencia entre cuentas propias",
                "Descripción",
                "Transferencias",
                Criticidad.ALTA,
                3L
        );
        CasoPruebaResponse response = new CasoPruebaResponse(
                104L,
                request.titulo(),
                request.descripcion(),
                request.modulo(),
                request.criticidad(),
                EstadoCasoPrueba.PENDIENTE,
                BigDecimal.ZERO,
                3L
        );

        when(casoPruebaService.crear(any(CasoPruebaRequest.class), eq("tester@banco.cl"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/casos-prueba")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(104))
                .andExpect(jsonPath("$.data.titulo").value("Validar transferencia entre cuentas propias"))
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.data.scorePrioridad").value(0))
                .andExpect(jsonPath("$.data.requisitoId").value(3));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void crear_deberiaRetornar400_cuandoTituloEsVacio() throws Exception {
        CasoPruebaRequest request = new CasoPruebaRequest("", "Desc", "Transferencias", Criticidad.ALTA, 3L);

        mockMvc.perform(post("/api/v1/casos-prueba")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errores.titulo").exists());
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void crear_deberiaRetornar400_cuandoCriticidadEsInvalida() throws Exception {
        String json = "{\"titulo\":\"Título\",\"descripcion\":\"Desc\",\"modulo\":\"Módulo\",\"criticidad\":\"CRITICA\",\"requisitoId\":3}";

        mockMvc.perform(post("/api/v1/casos-prueba")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DESARROLLADOR")
    void crear_deberiaRetornar403_cuandoRolNoEsQATesterNiAdministrador() throws Exception {
        CasoPruebaRequest request = new CasoPruebaRequest("Título", "Desc", "Módulo", Criticidad.ALTA, 3L);

        mockMvc.perform(post("/api/v1/casos-prueba")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar403_cuandoNoEstaAutenticado() throws Exception {
        CasoPruebaRequest request = new CasoPruebaRequest("Título", "Desc", "Módulo", Criticidad.ALTA, 3L);

        mockMvc.perform(post("/api/v1/casos-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

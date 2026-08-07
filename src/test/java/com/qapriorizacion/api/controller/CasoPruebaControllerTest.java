package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.request.CasoPruebaUpdateRequest;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                3L,
                null
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

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void listar_deberiaRetornar200YCuerpoSuccess() throws Exception {
        List<CasoPruebaResponse> casos = List.of(
                new CasoPruebaResponse(1L, "Caso 1", "Desc 1", "Módulo A", Criticidad.ALTA, EstadoCasoPrueba.PENDIENTE, BigDecimal.ZERO, 3L, null),
                new CasoPruebaResponse(2L, "Caso 2", null, "Módulo B", Criticidad.MEDIA, EstadoCasoPrueba.EN_CURSO, BigDecimal.ONE, 4L, null)
        );

        when(casoPruebaService.listar()).thenReturn(casos);

        mockMvc.perform(get("/api/v1/casos-prueba")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].titulo").value("Caso 1"))
                .andExpect(jsonPath("$.data[1].estado").value("EN_CURSO"));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void obtener_deberiaRetornar200YCuerpoSuccess() throws Exception {
        Long id = 104L;
        CasoPruebaResponse response = new CasoPruebaResponse(
                id, "Caso", "Desc", "Módulo", Criticidad.ALTA, EstadoCasoPrueba.PENDIENTE, BigDecimal.ZERO, 3L, null);

        when(casoPruebaService.obtener(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/casos-prueba/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(104))
                .andExpect(jsonPath("$.data.titulo").value("Caso"));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void actualizar_deberiaRetornar200YCuerpoSuccess() throws Exception {
        Long id = 104L;
        CasoPruebaUpdateRequest request = new CasoPruebaUpdateRequest(
                null, null, null, Criticidad.ALTA, EstadoCasoPrueba.EN_CURSO);
        CasoPruebaResponse response = new CasoPruebaResponse(
                id, "Caso", "Desc", "Módulo", Criticidad.ALTA, EstadoCasoPrueba.EN_CURSO, BigDecimal.valueOf(9.0), 3L, null);

        when(casoPruebaService.actualizar(eq(id), any(CasoPruebaUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/casos-prueba/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.data.scorePrioridad").value(9.0));
    }
}

package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse.CasoTrazabilidadResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse.RequisitoTrazabilidadResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse.UltimaEjecucionResponse;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.TrazabilidadService;
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

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrazabilidadController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class TrazabilidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @MockitoBean
    private TrazabilidadService trazabilidadService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void obtener_deberiaRetornar200ConTrazabilidadCompleta() throws Exception {
        TrazabilidadResponse response = new TrazabilidadResponse(
                new RequisitoTrazabilidadResponse(3L, "RF-03", "Transferencias entre cuentas propias"),
                new CasoTrazabilidadResponse(104L, "Validar transferencia entre cuentas propias"),
                new UltimaEjecucionResponse(501L, ResultadoEjecucion.FALLIDO, LocalDate.of(2026, 5, 21), "Falla"),
                EstadoCasoPrueba.BLOQUEADO
        );

        when(trazabilidadService.obtenerPorCasoPrueba(104L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trazabilidad/{casoPruebaId}", 104L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.requisito.codigo").value("RF-03"))
                .andExpect(jsonPath("$.data.casoPrueba.id").value(104))
                .andExpect(jsonPath("$.data.ultimaEjecucion.resultado").value("FALLIDO"))
                .andExpect(jsonPath("$.data.estadoActual").value("BLOQUEADO"));
    }

    @Test
    @WithMockUser(roles = "DESARROLLADOR")
    void obtener_deberiaRetornar200_cuandoRolEsDesarrollador() throws Exception {
        TrazabilidadResponse response = new TrazabilidadResponse(
                new RequisitoTrazabilidadResponse(3L, "RF-03", "Transferencias"),
                new CasoTrazabilidadResponse(104L, "Caso"),
                null,
                EstadoCasoPrueba.PENDIENTE
        );

        when(trazabilidadService.obtenerPorCasoPrueba(104L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trazabilidad/{casoPruebaId}", 104L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.casoPrueba.id").value(104));
    }

    @Test
    void obtener_deberiaRetornar401_cuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/trazabilidad/{casoPruebaId}", 104L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

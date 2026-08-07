package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.config.SecurityConfig;
import com.qapriorizacion.api.dto.response.RequisitoResponse;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.RequisitoService;
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

@WebMvcTest(RequisitoController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthFilter.class})
class RequisitoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequisitoService requisitoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void listar_deberiaRetornar200YCuerpoSuccess() throws Exception {
        List<RequisitoResponse> requisitos = List.of(
                new RequisitoResponse(1L, "RF-01", "Requisito 1", "Desc 1"),
                new RequisitoResponse(2L, "RF-02", "Requisito 2", "Desc 2")
        );

        when(requisitoService.listar()).thenReturn(requisitos);

        mockMvc.perform(get("/api/v1/requisitos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].codigo").value("RF-01"));
    }

    @Test
    @WithMockUser(roles = "DESARROLLADOR")
    void listar_deberiaRetornar403_cuandoRolNoEsAutorizado() throws Exception {
        mockMvc.perform(get("/api/v1/requisitos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_deberiaRetornar403_cuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/requisitos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

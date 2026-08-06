package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.qapriorizacion.api.dto.request.LoginRequest;
import com.qapriorizacion.api.dto.response.LoginResponse;
import com.qapriorizacion.api.dto.response.UsuarioResumenResponse;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    void login_deberiaRetornar200YCuerpoSuccess_cuandoRequestEsValida() throws Exception {
        LoginRequest request = new LoginRequest("usuario@ejemplo.com", "Password123");
        UsuarioResumenResponse resumen = new UsuarioResumenResponse(1L, "Usuario Ejemplo", RolUsuario.QA_TESTER);
        LoginResponse response = new LoginResponse("jwt-token", resumen);

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.usuario.nombre").value("Usuario Ejemplo"))
                .andExpect(jsonPath("$.data.usuario.rol").value("QA_TESTER"));
    }

    @Test
    @WithMockUser
    void login_deberiaRetornar400_cuandoCorreoEsInvalido() throws Exception {
        LoginRequest request = new LoginRequest("correo-no-valido", "Password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errores.correo").exists());
    }

    @Test
    @WithMockUser
    void login_deberiaRetornar400_cuandoPasswordEsCorto() throws Exception {
        LoginRequest request = new LoginRequest("usuario@ejemplo.com", "corta");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errores.password").exists());
    }

    @Test
    @WithMockUser
    void login_deberiaRetornar400_cuandoCamposSonVacios() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.password").exists());
    }
}

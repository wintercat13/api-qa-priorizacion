package com.qapriorizacion.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qapriorizacion.api.dto.request.UsuarioRequest;
import com.qapriorizacion.api.dto.response.UsuarioResponse;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.config.SecurityConfig;
import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthFilter.class, UsuarioControllerTest.TestConfig.class})
class UsuarioControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void crear_deberiaRetornar201YCuerpoSuccess_cuandoRequestEsValida() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Juan Soto", "j.soto@banco.cl", RolUsuario.QA_TESTER);
        UsuarioResponse response = new UsuarioResponse(5L, "Juan Soto", "j.soto@banco.cl", RolUsuario.QA_TESTER, true);

        when(usuarioService.crear(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.nombre").value("Juan Soto"))
                .andExpect(jsonPath("$.data.correo").value("j.soto@banco.cl"))
                .andExpect(jsonPath("$.data.rol").value("QA_TESTER"))
                .andExpect(jsonPath("$.data.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void crear_deberiaRetornar400_cuandoNombreEsVacio() throws Exception {
        UsuarioRequest request = new UsuarioRequest("", "j.soto@banco.cl", RolUsuario.QA_TESTER);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void crear_deberiaRetornar400_cuandoCorreoEsInvalido() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Juan Soto", "correo-no-valido", RolUsuario.QA_TESTER);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errores.correo").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void actualizar_deberiaRetornar200YCuerpoSuccess_cuandoRequestEsValida() throws Exception {
        Long id = 1L;
        UsuarioRequest request = new UsuarioRequest("C. Vera", "c.vera@banco.cl", RolUsuario.DESARROLLADOR);
        UsuarioResponse response = new UsuarioResponse(id, "C. Vera", "c.vera@banco.cl", RolUsuario.DESARROLLADOR, true);

        when(usuarioService.actualizar(id, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.rol").value("DESARROLLADOR"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void desactivar_deberiaRetornar200YCuerpoSuccess() throws Exception {
        Long id = 1L;
        UsuarioResponse response = new UsuarioResponse(id, "C. Vera", "c.vera@banco.cl", RolUsuario.DESARROLLADOR, false);

        when(usuarioService.desactivar(id)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/desactivar", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void listar_deberiaRetornar200YCuerpoSuccess() throws Exception {
        List<UsuarioResponse> usuarios = List.of(
                new UsuarioResponse(1L, "A", "a@banco.cl", RolUsuario.QA_TESTER, true),
                new UsuarioResponse(2L, "B", "b@banco.cl", RolUsuario.ADMINISTRADOR_QA, true)
        );

        when(usuarioService.listar()).thenReturn(usuarios);

        mockMvc.perform(get("/api/v1/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void crear_deberiaRetornar403_cuandoRolNoEsAdministrador() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Juan Soto", "j.soto@banco.cl", RolUsuario.QA_TESTER);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar403_cuandoNoEstaAutenticado() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Juan Soto", "j.soto@banco.cl", RolUsuario.QA_TESTER);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

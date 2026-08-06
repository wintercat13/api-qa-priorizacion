package com.qapriorizacion.api.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_deberiaPoblarContexto_cuandoTokenEsValido()
            throws ServletException, IOException {
        String token = "token-valido";
        String correo = "usuario@ejemplo.com";
        User user = new User(correo, "password",
                Collections.singletonList(() -> "ROLE_QA_TESTER"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extraerCorreo(token)).thenReturn(correo);
        when(userDetailsService.loadUserByUsername(correo)).thenReturn(user);
        when(jwtService.esTokenValido(token, user)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(correo);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_deberiaContinuarSinAutenticar_cuandoNoHayHeader()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_deberiaContinuarSinAutenticar_cuandoHeaderNoEmpiezaConBearer()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXN1YXJpbzpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_deberiaLimpiarContexto_cuandoTokenEsInvalido()
            throws ServletException, IOException {
        String token = "token-invalido";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extraerCorreo(token)).thenThrow(new JwtException("Token inválido"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_deberiaLimpiarContexto_cuandoUsuarioNoExiste()
            throws ServletException, IOException {
        String token = "token-valido";
        String correo = "noexiste@ejemplo.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extraerCorreo(token)).thenReturn(correo);
        when(userDetailsService.loadUserByUsername(correo))
                .thenThrow(new UsernameNotFoundException("Usuario no encontrado"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_noDeberiaReautenticar_cuandoContextoYaTieneAutenticacion()
            throws ServletException, IOException {
        String token = "token-valido";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "previo", null, Collections.emptyList()));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}

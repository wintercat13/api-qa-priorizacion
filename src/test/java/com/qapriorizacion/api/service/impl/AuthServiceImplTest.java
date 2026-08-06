package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.LoginRequest;
import com.qapriorizacion.api.dto.response.LoginResponse;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_deberiaRetornarTokenYUsuario_cuandoCredencialesSonValidas() {
        String correo = "usuario@ejemplo.com";
        String password = "Password123";
        String token = "jwt-token";
        LoginRequest request = new LoginRequest(correo, password);
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Usuario Ejemplo")
                .correo(correo)
                .passwordHash("hash")
                .rol(RolUsuario.QA_TESTER)
                .activo(true)
                .build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(correo, usuario.getRol().name())).thenReturn(token);

        LoginResponse response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(correo, password));
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.usuario().id()).isEqualTo(1L);
        assertThat(response.usuario().nombre()).isEqualTo("Usuario Ejemplo");
        assertThat(response.usuario().rol()).isEqualTo(RolUsuario.QA_TESTER);
    }

    @Test
    void login_deberiaLanzarBadCredentials_cuandoUsuarioNoExiste() {
        String correo = "noexiste@ejemplo.com";
        String password = "Password123";
        LoginRequest request = new LoginRequest(correo, password);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(correo, password));
    }
}

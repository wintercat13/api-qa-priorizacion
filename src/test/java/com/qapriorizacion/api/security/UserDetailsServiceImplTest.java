package com.qapriorizacion.api.security;

import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_deberiaRetornarUserDetails_cuandoUsuarioExisteYEstaActivo() {
        String correo = "activo@ejemplo.com";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Activo")
                .correo(correo)
                .passwordHash("hash")
                .rol(RolUsuario.ADMINISTRADOR_QA)
                .activo(true)
                .build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

        assertThat(userDetails.getUsername()).isEqualTo(correo);
        assertThat(userDetails.getPassword()).isEqualTo("hash");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .map(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMINISTRADOR_QA");
    }

    @Test
    void loadUserByUsername_deberiaRetornarUserDetailsDeshabilitado_cuandoUsuarioEstaInactivo() {
        String correo = "inactivo@ejemplo.com";
        Usuario usuario = Usuario.builder()
                .id(2L)
                .nombre("Inactivo")
                .correo(correo)
                .passwordHash("hash")
                .rol(RolUsuario.DESARROLLADOR)
                .activo(false)
                .build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_deberiaLanzarUsernameNotFoundException_cuandoUsuarioNoExiste() {
        String correo = "noexiste@ejemplo.com";

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(correo))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }
}

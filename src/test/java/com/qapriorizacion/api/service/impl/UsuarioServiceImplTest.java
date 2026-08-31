package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.UsuarioRequest;
import com.qapriorizacion.api.dto.response.UsuarioResponse;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.exception.CorreoDuplicadoException;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void crear_deberiaRetornarUsuarioActivo_cuandoRequestEsValida() {
        UsuarioRequest request = new UsuarioRequest("Juan Soto", "j.soto@banco.cl", RolUsuario.QA_TESTER);
        Usuario guardado = Usuario.builder()
                .id(5L)
                .nombre("Juan Soto")
                .correo("j.soto@banco.cl")
                .rol(RolUsuario.QA_TESTER)
                .activo(true)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);

        UsuarioResponse response = usuarioService.crear(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.nombre()).isEqualTo("Juan Soto");
        assertThat(response.correo()).isEqualTo("j.soto@banco.cl");
        assertThat(response.rol()).isEqualTo(RolUsuario.QA_TESTER);
        assertThat(response.activo()).isTrue();
    }

    @Test
    void crear_deberiaLanzarCorreoDuplicado_cuandoCorreoYaExiste() {
        UsuarioRequest request = new UsuarioRequest("Juan Soto", "j.soto@banco.cl", RolUsuario.QA_TESTER);

        when(usuarioRepository.save(any(Usuario.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> usuarioService.crear(request))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessage("Ya existe un usuario registrado con el correo indicado");
    }

    @Test
    void actualizar_deberiaModificarDatos_cuandoUsuarioExiste() {
        Long id = 1L;
        UsuarioRequest request = new UsuarioRequest("C. Vera", "c.vera@banco.cl", RolUsuario.DESARROLLADOR);
        Usuario existente = Usuario.builder()
                .id(id)
                .nombre("Carlos Vera")
                .correo("c.vera.old@banco.cl")
                .rol(RolUsuario.QA_TESTER)
                .activo(true)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.actualizar(id, request);

        assertThat(response.nombre()).isEqualTo("C. Vera");
        assertThat(response.correo()).isEqualTo("c.vera@banco.cl");
        assertThat(response.rol()).isEqualTo(RolUsuario.DESARROLLADOR);
    }

    @Test
    void desactivar_deberiaCambiarEstadoAInactivo_cuandoUsuarioExiste() {
        Long id = 1L;
        Usuario activo = Usuario.builder()
                .id(id)
                .nombre("C. Vera")
                .correo("c.vera@banco.cl")
                .rol(RolUsuario.DESARROLLADOR)
                .activo(true)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(activo));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.desactivar(id);

        assertThat(response.activo()).isFalse();
        verify(usuarioRepository).save(activo);
    }

    @Test
    void desactivar_deberiaLanzarRecursoNoEncontrado_cuandoUsuarioNoExiste() {
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.desactivar(id))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void listar_deberiaRetornarTodosLosUsuarios() {
        Usuario u1 = Usuario.builder().id(1L).nombre("A").correo("a@banco.cl").rol(RolUsuario.QA_TESTER).activo(true).build();
        Usuario u2 = Usuario.builder().id(2L).nombre("B").correo("b@banco.cl").rol(RolUsuario.ADMINISTRADOR_QA).activo(false).build();

        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UsuarioResponse> response = usuarioService.listar();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).activo()).isTrue();
        assertThat(response.get(1).activo()).isFalse();
    }
}

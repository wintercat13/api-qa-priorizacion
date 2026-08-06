package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.RequisitoRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasoPruebaServiceImplTest {

    @Mock
    private CasoPruebaRepository casoPruebaRepository;

    @Mock
    private RequisitoRepository requisitoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CasoPruebaServiceImpl casoPruebaService;

    @Test
    void crear_deberiaRetornarCasoEnEstadoPendiente_cuandoDatosSonValidos() {
        String correo = "tester@banco.cl";
        Usuario responsable = Usuario.builder().id(1L).correo(correo).rol(RolUsuario.QA_TESTER).activo(true).build();
        Requisito requisito = Requisito.builder().id(3L).codigo("RF-03").nombre("Requisito 3").build();
        CasoPruebaRequest request = new CasoPruebaRequest(
                "Validar transferencia entre cuentas propias",
                "Descripción",
                "Transferencias",
                Criticidad.ALTA,
                3L
        );
        CasoPrueba guardado = CasoPrueba.builder()
                .id(104L)
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .modulo(request.modulo())
                .criticidad(request.criticidad())
                .estado(EstadoCasoPrueba.PENDIENTE)
                .scorePrioridad(BigDecimal.ZERO)
                .responsable(responsable)
                .requisito(requisito)
                .build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(responsable));
        when(requisitoRepository.findById(3L)).thenReturn(Optional.of(requisito));
        when(casoPruebaRepository.save(any(CasoPrueba.class))).thenReturn(guardado);

        CasoPruebaResponse response = casoPruebaService.crear(request, correo);

        assertThat(response.id()).isEqualTo(104L);
        assertThat(response.titulo()).isEqualTo(request.titulo());
        assertThat(response.estado()).isEqualTo(EstadoCasoPrueba.PENDIENTE);
        assertThat(response.scorePrioridad()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.requisitoId()).isEqualTo(3L);
        verify(casoPruebaRepository).save(any(CasoPrueba.class));
    }

    @Test
    void crear_deberiaLanzarRecursoNoEncontrado_cuandoRequisitoNoExiste() {
        String correo = "tester@banco.cl";
        Usuario responsable = Usuario.builder().id(1L).correo(correo).rol(RolUsuario.QA_TESTER).activo(true).build();
        CasoPruebaRequest request = new CasoPruebaRequest("Título", "Desc", "Módulo", Criticidad.MEDIA, 99L);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(responsable));
        when(requisitoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoPruebaService.crear(request, correo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Requisito no encontrado");
    }

    @Test
    void crear_deberiaLanzarRecursoNoEncontrado_cuandoResponsableNoExiste() {
        String correo = "desconocido@banco.cl";
        CasoPruebaRequest request = new CasoPruebaRequest("Título", "Desc", "Módulo", Criticidad.MEDIA, 3L);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoPruebaService.crear(request, correo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario responsable no encontrado");
    }

    @Test
    void listar_deberiaRetornarTodosLosCasos() {
        Usuario responsable = Usuario.builder().id(1L).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba c1 = CasoPrueba.builder()
                .id(1L).titulo("Caso 1").descripcion("Desc 1").modulo("Módulo A")
                .criticidad(Criticidad.ALTA).estado(EstadoCasoPrueba.PENDIENTE)
                .scorePrioridad(BigDecimal.ZERO).responsable(responsable).requisito(requisito)
                .build();
        CasoPrueba c2 = CasoPrueba.builder()
                .id(2L).titulo("Caso 2").modulo("Módulo B")
                .criticidad(Criticidad.MEDIA).estado(EstadoCasoPrueba.EN_CURSO)
                .scorePrioridad(BigDecimal.ONE).responsable(responsable).requisito(requisito)
                .build();

        when(casoPruebaRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CasoPruebaResponse> response = casoPruebaService.listar();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).titulo()).isEqualTo("Caso 1");
        assertThat(response.get(1).estado()).isEqualTo(EstadoCasoPrueba.EN_CURSO);
    }
}

package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.request.CasoPruebaUpdateRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.TransicionEstadoInvalidaException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.RequisitoRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.PrioridadService;
import com.qapriorizacion.api.validation.TransicionEstadoValidator;
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

    @Mock
    private TransicionEstadoValidator transicionEstadoValidator;

    @Mock
    private PrioridadService prioridadService;

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

    @Test
    void actualizar_deberiaCambiarEstadoYRecalcularScore_cuandoTransicionEsValida() {
        Long id = 104L;
        CasoPruebaUpdateRequest request = new CasoPruebaUpdateRequest(
                null, null, null, Criticidad.ALTA, EstadoCasoPrueba.EN_CURSO);
        Usuario responsable = Usuario.builder().id(1L).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(id).titulo("Caso").modulo("Módulo").criticidad(Criticidad.MEDIA)
                .estado(EstadoCasoPrueba.PENDIENTE).scorePrioridad(BigDecimal.valueOf(6.0))
                .contadorFallos(0).responsable(responsable).requisito(requisito)
                .build();

        when(casoPruebaRepository.findById(id)).thenReturn(Optional.of(caso));
        when(transicionEstadoValidator.esValida(EstadoCasoPrueba.PENDIENTE, EstadoCasoPrueba.EN_CURSO)).thenReturn(true);
        when(prioridadService.calcularScore(Criticidad.ALTA, 0)).thenReturn(BigDecimal.valueOf(9.0));
        when(casoPruebaRepository.save(any(CasoPrueba.class))).thenAnswer(inv -> inv.getArgument(0));

        CasoPruebaResponse response = casoPruebaService.actualizar(id, request);

        assertThat(response.estado()).isEqualTo(EstadoCasoPrueba.EN_CURSO);
        assertThat(response.criticidad()).isEqualTo(Criticidad.ALTA);
        assertThat(response.scorePrioridad()).isEqualByComparingTo(BigDecimal.valueOf(9.0));
    }

    @Test
    void actualizar_deberiaLanzarExcepcion_cuandoTransicionEsInvalida() {
        Long id = 104L;
        CasoPruebaUpdateRequest request = new CasoPruebaUpdateRequest(
                null, null, null, Criticidad.MEDIA, EstadoCasoPrueba.EJECUTADO);
        Usuario responsable = Usuario.builder().id(1L).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(id).titulo("Caso").modulo("Módulo").criticidad(Criticidad.MEDIA)
                .estado(EstadoCasoPrueba.PENDIENTE).scorePrioridad(BigDecimal.valueOf(6.0))
                .responsable(responsable).requisito(requisito)
                .build();

        when(casoPruebaRepository.findById(id)).thenReturn(Optional.of(caso));
        when(transicionEstadoValidator.esValida(EstadoCasoPrueba.PENDIENTE, EstadoCasoPrueba.EJECUTADO)).thenReturn(false);

        assertThatThrownBy(() -> casoPruebaService.actualizar(id, request))
                .isInstanceOf(TransicionEstadoInvalidaException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }

    @Test
    void obtener_deberiaRetornarCaso_cuandoExiste() {
        Long id = 104L;
        Usuario responsable = Usuario.builder().id(1L).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(id).titulo("Caso").modulo("Módulo").criticidad(Criticidad.ALTA)
                .estado(EstadoCasoPrueba.PENDIENTE).scorePrioridad(BigDecimal.ZERO)
                .responsable(responsable).requisito(requisito)
                .build();

        when(casoPruebaRepository.findById(id)).thenReturn(Optional.of(caso));

        CasoPruebaResponse response = casoPruebaService.obtener(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.titulo()).isEqualTo("Caso");
    }

    @Test
    void obtener_deberiaLanzarRecursoNoEncontrado_cuandoNoExiste() {
        Long id = 999L;
        when(casoPruebaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoPruebaService.obtener(id))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Caso de prueba no encontrado");
    }
}

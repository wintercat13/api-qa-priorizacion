package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.EjecucionRequest;
import com.qapriorizacion.api.dto.response.EjecucionResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import com.qapriorizacion.api.entity.enums.RolUsuario;
import com.qapriorizacion.api.entity.Ejecucion;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.TransicionEstadoInvalidaException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.PrioridadService;
import com.qapriorizacion.api.validation.TransicionEstadoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EjecucionServiceImplTest {

    @Mock
    private EjecucionRepository ejecucionRepository;

    @Mock
    private CasoPruebaRepository casoPruebaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TransicionEstadoValidator transicionEstadoValidator;

    @Mock
    private PrioridadService prioridadService;

    @InjectMocks
    private EjecucionServiceImpl ejecucionService;

    @Test
    void registrar_deberiaCambiarEstadoAEjecutado_cuandoResultadoEsAprobado() {
        String correo = "tester@banco.cl";
        Usuario ejecutor = Usuario.builder().id(1L).correo(correo).rol(RolUsuario.QA_TESTER).activo(true).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(104L).titulo("Caso").modulo("Módulo").criticidad(Criticidad.ALTA)
                .estado(EstadoCasoPrueba.EN_CURSO).contadorFallos(0).scorePrioridad(BigDecimal.ZERO)
                .responsable(ejecutor).requisito(requisito)
                .build();
        EjecucionRequest request = new EjecucionRequest(104L, ResultadoEjecucion.APROBADO, "OK");

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(ejecutor));
        when(casoPruebaRepository.findById(104L)).thenReturn(Optional.of(caso));
        when(transicionEstadoValidator.esValida(EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.EJECUTADO)).thenReturn(true);
        when(prioridadService.calcularScore(Criticidad.ALTA, 0)).thenReturn(new BigDecimal("9.00"));
        when(ejecucionRepository.save(any())).thenAnswer(inv -> {
            Ejecucion e = inv.getArgument(0);
            e.setId(501L);
            return e;
        });

        EjecucionResponse response = ejecucionService.registrar(request, correo);

        assertThat(response.resultado()).isEqualTo(ResultadoEjecucion.APROBADO);
        assertThat(response.estadoCaso()).isEqualTo(EstadoCasoPrueba.EJECUTADO);
        assertThat(caso.getEstado()).isEqualTo(EstadoCasoPrueba.EJECUTADO);
    }

    @Test
    void registrar_deberiaCambiarEstadoABloqueadoEIncrementarFallos_cuandoResultadoEsFallido() {
        String correo = "tester@banco.cl";
        Usuario ejecutor = Usuario.builder().id(1L).correo(correo).rol(RolUsuario.QA_TESTER).activo(true).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(104L).titulo("Caso").modulo("Módulo").criticidad(Criticidad.ALTA)
                .estado(EstadoCasoPrueba.EN_CURSO).contadorFallos(1).scorePrioridad(BigDecimal.ZERO)
                .responsable(ejecutor).requisito(requisito)
                .build();
        EjecucionRequest request = new EjecucionRequest(104L, ResultadoEjecucion.FALLIDO, "Falla");

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(ejecutor));
        when(casoPruebaRepository.findById(104L)).thenReturn(Optional.of(caso));
        when(transicionEstadoValidator.esValida(EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.BLOQUEADO)).thenReturn(true);
        when(prioridadService.calcularScore(Criticidad.ALTA, 2)).thenReturn(new BigDecimal("9.50"));
        when(ejecucionRepository.save(any())).thenAnswer(inv -> {
            Ejecucion e = inv.getArgument(0);
            e.setId(502L);
            return e;
        });

        EjecucionResponse response = ejecucionService.registrar(request, correo);

        assertThat(response.resultado()).isEqualTo(ResultadoEjecucion.FALLIDO);
        assertThat(response.estadoCaso()).isEqualTo(EstadoCasoPrueba.BLOQUEADO);
        assertThat(caso.getContadorFallos()).isEqualTo(2);
    }

    @Test
    void registrar_deberiaLanzarExcepcion_cuandoTransicionEsInvalida() {
        String correo = "tester@banco.cl";
        Usuario ejecutor = Usuario.builder().id(1L).correo(correo).rol(RolUsuario.QA_TESTER).activo(true).build();
        Requisito requisito = Requisito.builder().id(3L).build();
        CasoPrueba caso = CasoPrueba.builder()
                .id(104L).titulo("Caso").modulo("Módulo").criticidad(Criticidad.ALTA)
                .estado(EstadoCasoPrueba.ARCHIVADO).contadorFallos(0)
                .responsable(ejecutor).requisito(requisito)
                .build();
        EjecucionRequest request = new EjecucionRequest(104L, ResultadoEjecucion.APROBADO, "OK");

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(ejecutor));
        when(casoPruebaRepository.findById(104L)).thenReturn(Optional.of(caso));
        when(transicionEstadoValidator.esValida(EstadoCasoPrueba.ARCHIVADO, EstadoCasoPrueba.EJECUTADO)).thenReturn(false);

        assertThatThrownBy(() -> ejecucionService.registrar(request, correo))
                .isInstanceOf(TransicionEstadoInvalidaException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }

    @Test
    void registrar_deberiaLanzarExcepcion_cuandoCasoNoExiste() {
        String correo = "tester@banco.cl";
        Usuario ejecutor = Usuario.builder().id(1L).correo(correo).rol(RolUsuario.QA_TESTER).activo(true).build();
        EjecucionRequest request = new EjecucionRequest(999L, ResultadoEjecucion.APROBADO, "OK");

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(ejecutor));
        when(casoPruebaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ejecucionService.registrar(request, correo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Caso de prueba no encontrado");
    }
}

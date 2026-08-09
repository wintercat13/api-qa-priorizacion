package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.EjecucionRequest;
import com.qapriorizacion.api.dto.response.EjecucionResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Ejecucion;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.TransicionEstadoInvalidaException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.EjecucionService;
import com.qapriorizacion.api.service.PrioridadService;
import com.qapriorizacion.api.validation.TransicionEstadoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EjecucionServiceImpl implements EjecucionService {

    private final EjecucionRepository ejecucionRepository;
    private final CasoPruebaRepository casoPruebaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransicionEstadoValidator transicionEstadoValidator;
    private final PrioridadService prioridadService;

    @Override
    @Transactional
    public EjecucionResponse registrar(EjecucionRequest request, String correoEjecutor) {
        Usuario ejecutor = usuarioRepository.findByCorreo(correoEjecutor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario ejecutor no encontrado"));

        CasoPrueba caso = casoPruebaRepository.findById(request.casoPruebaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));

        EstadoCasoPrueba nuevoEstado = switch (request.resultado()) {
            case APROBADO -> EstadoCasoPrueba.EJECUTADO;
            case FALLIDO -> EstadoCasoPrueba.BLOQUEADO;
            case BLOQUEADO -> EstadoCasoPrueba.BLOQUEADO;
        };

        if (!transicionEstadoValidator.esValida(caso.getEstado(), nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                    "Transición de estado no permitida: " + caso.getEstado() + " -> " + nuevoEstado);
        }

        caso.setEstado(nuevoEstado);
        if (request.resultado() == ResultadoEjecucion.FALLIDO) {
            caso.setContadorFallos(caso.getContadorFallos() + 1);
        }
        BigDecimal nuevoScore = prioridadService.calcularScore(caso.getCriticidad(), caso.getContadorFallos());
        caso.setScorePrioridad(nuevoScore);
        caso.setFechaUltimaActividad(OffsetDateTime.now());
        casoPruebaRepository.save(caso);

        Ejecucion ejecucion = Ejecucion.builder()
                .casoPrueba(caso)
                .ejecutor(ejecutor)
                .resultado(request.resultado())
                .observaciones(request.observaciones())
                .build();

        Ejecucion guardada = ejecucionRepository.save(ejecucion);
        return mapear(guardada);
    }

    private EjecucionResponse mapear(Ejecucion ejecucion) {
        return new EjecucionResponse(
                ejecucion.getId(),
                ejecucion.getCasoPrueba().getId(),
                ejecucion.getResultado(),
                ejecucion.getCasoPrueba().getEstado(),
                ejecucion.getObservaciones(),
                ejecucion.getFechaEjecucion()
        );
    }
}

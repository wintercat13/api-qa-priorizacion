package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.TrazabilidadResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse.CasoTrazabilidadResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse.RequisitoTrazabilidadResponse;
import com.qapriorizacion.api.dto.response.TrazabilidadResponse.UltimaEjecucionResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Ejecucion;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.service.TrazabilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrazabilidadServiceImpl implements TrazabilidadService {

    private final CasoPruebaRepository casoPruebaRepository;
    private final EjecucionRepository ejecucionRepository;

    @Override
    @Transactional(readOnly = true)
    public TrazabilidadResponse obtenerPorCasoPrueba(Long casoPruebaId) {
        CasoPrueba caso = casoPruebaRepository.findById(casoPruebaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));

        Optional<Ejecucion> ultimaEjecucion = ejecucionRepository
                .findTopByCasoPruebaIdOrderByFechaEjecucionDesc(casoPruebaId);

        return new TrazabilidadResponse(
                new RequisitoTrazabilidadResponse(
                        caso.getRequisito().getId(),
                        caso.getRequisito().getCodigo(),
                        caso.getRequisito().getNombre()),
                new CasoTrazabilidadResponse(
                        caso.getId(),
                        caso.getTitulo()),
                ultimaEjecucion.map(this::mapearUltimaEjecucion).orElse(null),
                caso.getEstado()
        );
    }

    private UltimaEjecucionResponse mapearUltimaEjecucion(Ejecucion ejecucion) {
        return new UltimaEjecucionResponse(
                ejecucion.getId(),
                ejecucion.getResultado(),
                ejecucion.getFechaEjecucion().toLocalDate(),
                ejecucion.getObservaciones()
        );
    }
}

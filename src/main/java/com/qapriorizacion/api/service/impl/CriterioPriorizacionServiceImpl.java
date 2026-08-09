package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.ConfiguracionCriteriosRequest;
import com.qapriorizacion.api.dto.request.CriterioPriorizacionRequest;
import com.qapriorizacion.api.dto.response.ConfiguracionCriteriosResponse;
import com.qapriorizacion.api.dto.response.CriterioPriorizacionResponse;
import com.qapriorizacion.api.entity.CriterioPriorizacion;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.SumaPesosInvalidaException;
import com.qapriorizacion.api.repository.CriterioPriorizacionRepository;
import com.qapriorizacion.api.service.CriterioPriorizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CriterioPriorizacionServiceImpl implements CriterioPriorizacionService {

    private static final BigDecimal TOLERANCIA = new BigDecimal("0.001");

    private final CriterioPriorizacionRepository criterioRepository;
    private final CasoPruebaServiceImpl casoPruebaService;

    @Override
    @Transactional(readOnly = true)
    public List<CriterioPriorizacionResponse> listar() {
        return criterioRepository.findAll().stream()
                .map(this::mapear)
                .toList();
    }

    @Override
    @Transactional
    public ConfiguracionCriteriosResponse actualizar(ConfiguracionCriteriosRequest request) {
        validarSumaPesos(request.criterios());

        List<CriterioPriorizacion> actualizados = new ArrayList<>();
        for (CriterioPriorizacionRequest dto : request.criterios()) {
            CriterioPriorizacion criterio = criterioRepository.findById(dto.id())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Criterio de priorización no encontrado: " + dto.id()));

            criterio.setNombre(dto.nombre().trim());
            criterio.setDescripcion(dto.descripcion());
            criterio.setPeso(dto.peso());
            criterio.setActivo(dto.activo());
            actualizados.add(criterioRepository.save(criterio));
        }

        int casosRecalculados = casoPruebaService.recalcularScores();
        return new ConfiguracionCriteriosResponse(actualizados.size(), casosRecalculados);
    }

    private void validarSumaPesos(List<CriterioPriorizacionRequest> criterios) {
        BigDecimal suma = criterios.stream()
                .filter(CriterioPriorizacionRequest::activo)
                .map(CriterioPriorizacionRequest::peso)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (suma.subtract(BigDecimal.ONE).abs().compareTo(TOLERANCIA) > 0) {
            throw new SumaPesosInvalidaException(
                    "La suma de los pesos de los criterios activos debe ser 1.0 (100%). Suma actual: " + suma);
        }
    }

    private CriterioPriorizacionResponse mapear(CriterioPriorizacion criterio) {
        return new CriterioPriorizacionResponse(
                criterio.getId(),
                criterio.getNombre(),
                criterio.getDescripcion(),
                criterio.getPeso(),
                criterio.isActivo(),
                criterio.getFechaActualizacion()
        );
    }
}

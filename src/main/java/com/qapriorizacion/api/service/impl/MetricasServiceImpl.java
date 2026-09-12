package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.service.MetricasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricasServiceImpl implements MetricasService {

    private final CasoPruebaRepository casoPruebaRepository;
    private final EjecucionRepository ejecucionRepository;

    @Override
    @Transactional(readOnly = true)
    public MetricasResponse calcularMetricas(LocalDate desde, LocalDate hasta) {
        long totalActivos = casoPruebaRepository.countByEstadoNot(EstadoCasoPrueba.ARCHIVADO);
        long ejecutados = casoPruebaRepository.countByEstado(EstadoCasoPrueba.EJECUTADO);
        long enCurso = casoPruebaRepository.countByEstado(EstadoCasoPrueba.EN_CURSO);
        long bloqueados = casoPruebaRepository.countByEstado(EstadoCasoPrueba.BLOQUEADO);
        long pendientes = casoPruebaRepository.countByEstado(EstadoCasoPrueba.PENDIENTE);
        long obsoletos = casoPruebaRepository.countByEstado(EstadoCasoPrueba.OBSOLETO);
        long obsoletosDepurados = casoPruebaRepository.countByEstado(EstadoCasoPrueba.ARCHIVADO);

        int cobertura = calcularPorcentaje(ejecutados, totalActivos + obsoletosDepurados);
        int porcentajeEjecutado = calcularPorcentaje(ejecutados, totalActivos);
        int cumplimientoSLA = calcularCumplimientoSLA(ejecutados, enCurso, bloqueados, pendientes);

        return new MetricasResponse(
                cobertura,
                porcentajeEjecutado,
                (int) obsoletosDepurados,
                cumplimientoSLA
        );
    }

    private int calcularPorcentaje(long numerador, long denominador) {
        if (denominador == 0) {
            return 0;
        }
        return (int) Math.round((double) numerador / denominador * 100);
    }

    private int calcularCumplimientoSLA(long ejecutados, long enCurso, long bloqueados, long pendientes) {
        long totalActivos = ejecutados + enCurso + bloqueados + pendientes;
        if (totalActivos == 0) {
            return 0;
        }
        long dentroSLA = ejecutados + enCurso;
        return (int) Math.round((double) dentroSLA / totalActivos * 100);
    }
}

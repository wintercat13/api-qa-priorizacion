package com.qapriorizacion.api.dto.response;

public record MetricasResponse(
        int cobertura,
        int porcentajeEjecutado,
        int casosObsoletosDepurados,
        int cumplimientoSLA
) {
}

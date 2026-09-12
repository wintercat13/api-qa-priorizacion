package com.qapriorizacion.api.dto.response;

public record DashboardKpiResponse(
        long totalCasos,
        long prioridadAlta,
        int porcentajeEjecutados,
        long duplicadosDetectados
) {
}

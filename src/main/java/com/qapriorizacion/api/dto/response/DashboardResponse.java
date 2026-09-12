package com.qapriorizacion.api.dto.response;

import java.util.List;

public record DashboardResponse(
        DashboardKpiResponse kpis,
        List<SerieResponse> casosPorModulo,
        List<EstadoEjecucionResponse> estadoEjecucion
) {
}

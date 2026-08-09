package com.qapriorizacion.api.dto.response;

public record ConfiguracionCriteriosResponse(
        int criteriosActualizados,
        int casosRecalculados
) {
}

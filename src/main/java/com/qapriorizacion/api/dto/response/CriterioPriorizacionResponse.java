package com.qapriorizacion.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CriterioPriorizacionResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal peso,
        boolean activo,
        OffsetDateTime fechaActualizacion
) {
}

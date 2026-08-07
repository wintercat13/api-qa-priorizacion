package com.qapriorizacion.api.dto.response;

import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CasoPruebaResponse(
        Long id,
        String titulo,
        String descripcion,
        String modulo,
        Criticidad criticidad,
        EstadoCasoPrueba estado,
        BigDecimal scorePrioridad,
        Long requisitoId,
        OffsetDateTime fechaActualizacion
) {
}

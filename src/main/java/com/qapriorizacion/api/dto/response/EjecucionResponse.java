package com.qapriorizacion.api.dto.response;

import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;

import java.time.OffsetDateTime;

public record EjecucionResponse(
        Long id,
        Long casoPruebaId,
        ResultadoEjecucion resultado,
        EstadoCasoPrueba estadoCaso,
        String observaciones,
        OffsetDateTime fechaEjecucion
) {
}

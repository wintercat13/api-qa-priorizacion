package com.qapriorizacion.api.dto.response;

import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;

import java.time.LocalDate;

public record TrazabilidadResponse(
        RequisitoTrazabilidadResponse requisito,
        CasoTrazabilidadResponse casoPrueba,
        UltimaEjecucionResponse ultimaEjecucion,
        EstadoCasoPrueba estadoActual
) {

    public record RequisitoTrazabilidadResponse(
            Long id,
            String codigo,
            String nombre
    ) {
    }

    public record CasoTrazabilidadResponse(
            Long id,
            String titulo
    ) {
    }

    public record UltimaEjecucionResponse(
            Long id,
            ResultadoEjecucion resultado,
            LocalDate fecha,
            String observaciones
    ) {
    }
}

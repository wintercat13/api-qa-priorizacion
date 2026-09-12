package com.qapriorizacion.api.dto.response;

public record EstadoEjecucionResponse(
        String estado,
        long cantidad,
        int porcentaje
) {
}

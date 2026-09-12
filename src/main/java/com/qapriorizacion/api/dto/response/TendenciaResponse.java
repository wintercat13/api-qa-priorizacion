package com.qapriorizacion.api.dto.response;

public record TendenciaResponse(
        String periodo,
        long ejecutados,
        long pendientes,
        long bloqueados
) {
}

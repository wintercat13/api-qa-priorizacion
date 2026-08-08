package com.qapriorizacion.api.dto.response;

import java.math.BigDecimal;

public record CasoPriorizadoResponse(
        Long id,
        String titulo,
        String modulo,
        BigDecimal scorePrioridad
) {
}

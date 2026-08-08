package com.qapriorizacion.api.dto.response;

import java.time.LocalDate;

public record CasoObsoletoResponse(
        Long id,
        String titulo,
        LocalDate ultimaActividad
) {
}

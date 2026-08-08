package com.qapriorizacion.api.dto.response;

import java.math.BigDecimal;

public record DuplicidadResponse(
        boolean posibleDuplicado,
        Long casoSimilarId,
        BigDecimal porcentajeSimilitud
) {
}

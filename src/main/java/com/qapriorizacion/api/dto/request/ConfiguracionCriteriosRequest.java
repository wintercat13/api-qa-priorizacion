package com.qapriorizacion.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConfiguracionCriteriosRequest(
        @NotEmpty(message = "Debe incluir al menos un criterio")
        @Valid
        List<CriterioPriorizacionRequest> criterios
) {
}

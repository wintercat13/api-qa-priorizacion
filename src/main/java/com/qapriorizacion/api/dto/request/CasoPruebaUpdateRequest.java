package com.qapriorizacion.api.dto.request;

import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import jakarta.validation.constraints.NotNull;

public record CasoPruebaUpdateRequest(
        String titulo,
        String descripcion,
        String modulo,

        @NotNull(message = "La criticidad es obligatoria")
        Criticidad criticidad,

        @NotNull(message = "El estado es obligatorio")
        EstadoCasoPrueba estado
) {
}

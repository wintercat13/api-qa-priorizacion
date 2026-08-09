package com.qapriorizacion.api.dto.request;

import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EjecucionRequest(
        @NotNull(message = "El caso de prueba es obligatorio")
        Long casoPruebaId,

        @NotNull(message = "El resultado de la ejecución es obligatorio")
        ResultadoEjecucion resultado,

        @Size(max = 500, message = "Las observaciones no pueden exceder los 500 caracteres")
        String observaciones
) {
}

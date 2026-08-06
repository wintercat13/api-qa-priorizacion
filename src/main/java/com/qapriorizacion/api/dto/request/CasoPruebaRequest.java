package com.qapriorizacion.api.dto.request;

import com.qapriorizacion.api.entity.enums.Criticidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CasoPruebaRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede exceder los 150 caracteres")
        String titulo,

        @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
        String descripcion,

        @NotBlank(message = "El módulo es obligatorio")
        @Size(max = 100, message = "El módulo no puede exceder los 100 caracteres")
        String modulo,

        @NotNull(message = "La criticidad es obligatoria")
        Criticidad criticidad,

        @NotNull(message = "El requisito asociado es obligatorio")
        Long requisitoId
) {
}

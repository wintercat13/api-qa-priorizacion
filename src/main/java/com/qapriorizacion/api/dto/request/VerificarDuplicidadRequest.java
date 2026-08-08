package com.qapriorizacion.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificarDuplicidadRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede exceder los 150 caracteres")
        String titulo,

        @NotBlank(message = "El módulo es obligatorio")
        @Size(max = 100, message = "El módulo no puede exceder los 100 caracteres")
        String modulo
) {
}

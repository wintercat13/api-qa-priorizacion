package com.qapriorizacion.api.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriterioPriorizacionRequest(
        Long id,

        @NotBlank(message = "El nombre del criterio es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String nombre,

        @Size(max = 300, message = "La descripción no puede exceder los 300 caracteres")
        String descripcion,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El peso debe ser mayor o igual a 0")
        @DecimalMax(value = "1.0", inclusive = true, message = "El peso debe ser menor o igual a 1")
        BigDecimal peso,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {
}

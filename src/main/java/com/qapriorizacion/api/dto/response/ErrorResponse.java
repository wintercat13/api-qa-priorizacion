package com.qapriorizacion.api.dto.response;

import java.util.Map;

public record ErrorResponse(String status, String message, Map<String, String> errores) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse("error", message, null);
    }

    public static ErrorResponse of(String message, Map<String, String> errores) {
        return new ErrorResponse("error", message, errores);
    }
}

package com.qapriorizacion.api.dto.response;

public record ApiResponse<T>(String status, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data);
    }
}

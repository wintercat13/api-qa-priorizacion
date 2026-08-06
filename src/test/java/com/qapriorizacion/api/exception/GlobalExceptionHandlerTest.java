package com.qapriorizacion.api.exception;

import com.qapriorizacion.api.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_deberiaRetornarErroresPorCampo() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "correo", "El correo es obligatorio"));
        bindingResult.addError(new FieldError("target", "password", "La contraseña es obligatoria"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getMethods()[0], -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("error");
        assertThat(response.getBody().message()).isEqualTo("Datos de entrada inválidos");
        assertThat(response.getBody().errores()).containsEntry("correo", "El correo es obligatorio");
        assertThat(response.getBody().errores()).containsEntry("password", "La contraseña es obligatoria");
    }

    @Test
    void handleBadCredentials_deberiaRetornarUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Credenciales inválidas");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Credenciales inválidas");
        assertThat(response.getBody().errores()).isNull();
    }

    @Test
    void handleDisabled_deberiaRetornarForbidden() {
        DisabledException ex = new DisabledException("Cuenta inactiva");

        ResponseEntity<ErrorResponse> response = handler.handleDisabled(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("La cuenta se encuentra inactiva");
    }

    @Test
    void handleAccessDenied_deberiaRetornarForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Acceso denegado");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Permisos insuficientes para acceder a este recurso");
    }
}

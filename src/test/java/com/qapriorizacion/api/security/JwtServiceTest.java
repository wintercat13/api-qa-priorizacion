package com.qapriorizacion.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "dev-only-secret-change-me-dev-only-secret-change-me";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 30);
    }

    @Test
    void generarToken_deberiaIncluirCorreoYRol() {
        String token = jwtService.generarToken("usuario@ejemplo.com", "QA_TESTER");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extraerCorreo(token)).isEqualTo("usuario@ejemplo.com");
    }

    @Test
    void esTokenValido_deberiaRetornarTrue_cuandoCorreoCoincideYTokenNoExpira() {
        String correo = "usuario@ejemplo.com";
        String token = jwtService.generarToken(correo, "QA_TESTER");
        UserDetails userDetails = new User(correo, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_QA_TESTER")));

        boolean valido = jwtService.esTokenValido(token, userDetails);

        assertThat(valido).isTrue();
    }

    @Test
    void esTokenValido_deberiaRetornarFalse_cuandoCorreoNoCoincide() {
        String token = jwtService.generarToken("usuario@ejemplo.com", "QA_TESTER");
        UserDetails userDetails = new User("otro@ejemplo.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_QA_TESTER")));

        boolean valido = jwtService.esTokenValido(token, userDetails);

        assertThat(valido).isFalse();
    }

    @Test
    void esTokenValido_deberiaRetornarFalse_cuandoTokenEstaExpirado() throws InterruptedException {
        JwtService serviceExpirado = new JwtService(SECRET, 0);
        String token = serviceExpirado.generarToken("usuario@ejemplo.com", "QA_TESTER");
        UserDetails userDetails = new User("usuario@ejemplo.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_QA_TESTER")));

        Thread.sleep(1100);

        boolean valido = jwtService.esTokenValido(token, userDetails);

        assertThat(valido).isFalse();
    }

    @Test
    void extraerCorreo_deberiaLanzarExcepcion_cuandoTokenEstaExpirado() throws InterruptedException {
        JwtService serviceExpirado = new JwtService(SECRET, 0);
        String token = serviceExpirado.generarToken("usuario@ejemplo.com", "QA_TESTER");

        Thread.sleep(1100);

        assertThatThrownBy(() -> jwtService.extraerCorreo(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void esTokenValido_deberiaRetornarFalse_cuandoTokenEstaExpiradoYHayMargenDeReloj() {
        JwtService serviceExpirado = new JwtService(SECRET, 0);
        String token = serviceExpirado.generarToken("usuario@ejemplo.com", "QA_TESTER");
        UserDetails userDetails = new User("usuario@ejemplo.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_QA_TESTER")));

        boolean valido = jwtService.esTokenValido(token, userDetails);

        assertThat(valido).isFalse();
    }

    @Test
    void generarToken_deberiaTenerFechaExpiracionPosteriorALaEmision() {
        Instant antes = Instant.now();
        String token = jwtService.generarToken("usuario@ejemplo.com", "QA_TESTER");
        Date expiracion = io.jsonwebtoken.Jwts.parser()
                .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        assertThat(expiracion.toInstant()).isAfter(antes);
        assertThat(expiracion.toInstant()).isBefore(Instant.now().plus(31, ChronoUnit.MINUTES));
    }
}

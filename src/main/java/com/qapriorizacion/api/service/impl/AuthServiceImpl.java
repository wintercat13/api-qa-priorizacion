package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.LoginRequest;
import com.qapriorizacion.api.dto.response.LoginResponse;
import com.qapriorizacion.api.dto.response.UsuarioResumenResponse;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.password()));

        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        String token = jwtService.generarToken(usuario.getCorreo(), usuario.getRol().name());
        UsuarioResumenResponse resumen = new UsuarioResumenResponse(
                usuario.getId(), usuario.getNombre(), usuario.getRol());

        return new LoginResponse(token, resumen);
    }
}

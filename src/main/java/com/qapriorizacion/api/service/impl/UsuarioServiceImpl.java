package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.UsuarioRequest;
import com.qapriorizacion.api.dto.response.UsuarioResponse;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.exception.CorreoDuplicadoException;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .correo(request.correo().trim().toLowerCase())
                .rol(request.rol())
                .activo(true)
                .passwordHash("PLACEHOLDER")
                .build();

        try {
            Usuario guardado = usuarioRepository.save(usuario);
            return mapear(guardado);
        } catch (DataIntegrityViolationException ex) {
            throw new CorreoDuplicadoException("Ya existe un usuario registrado con el correo indicado");
        }
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(request.correo().trim().toLowerCase());
        usuario.setRol(request.rol());

        try {
            Usuario guardado = usuarioRepository.save(usuario);
            return mapear(guardado);
        } catch (DataIntegrityViolationException ex) {
            throw new CorreoDuplicadoException("Ya existe un usuario registrado con el correo indicado");
        }
    }

    @Override
    @Transactional
    public UsuarioResponse desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        usuario.setActivo(false);
        return mapear(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::mapear)
                .toList();
    }

    private UsuarioResponse mapear(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }
}

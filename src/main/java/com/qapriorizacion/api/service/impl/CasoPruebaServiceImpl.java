package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.request.CasoPruebaUpdateRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.TransicionEstadoInvalidaException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.RequisitoRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.CasoPruebaService;
import com.qapriorizacion.api.service.PrioridadService;
import com.qapriorizacion.api.validation.TransicionEstadoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CasoPruebaServiceImpl implements CasoPruebaService {

    private final CasoPruebaRepository casoPruebaRepository;
    private final RequisitoRepository requisitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransicionEstadoValidator transicionEstadoValidator;
    private final PrioridadService prioridadService;

    @Override
    @Transactional
    public CasoPruebaResponse crear(CasoPruebaRequest request, String correoResponsable) {
        Usuario responsable = usuarioRepository.findByCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario responsable no encontrado"));

        Requisito requisito = requisitoRepository.findById(request.requisitoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Requisito no encontrado"));

        CasoPrueba caso = CasoPrueba.builder()
                .titulo(request.titulo().trim())
                .descripcion(request.descripcion())
                .modulo(request.modulo().trim())
                .criticidad(request.criticidad())
                .estado(EstadoCasoPrueba.PENDIENTE)
                .scorePrioridad(BigDecimal.ZERO)
                .posibleDuplicado(false)
                .contadorFallos(0)
                .responsable(responsable)
                .requisito(requisito)
                .build();

        CasoPrueba guardado = casoPruebaRepository.save(caso);
        return mapear(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CasoPruebaResponse> listar() {
        return casoPruebaRepository.findAll().stream()
                .map(this::mapear)
                .toList();
    }

    @Override
    @Transactional
    public CasoPruebaResponse actualizar(Long id, CasoPruebaUpdateRequest request) {
        CasoPrueba caso = casoPruebaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));

        if (!transicionEstadoValidator.esValida(caso.getEstado(), request.estado())) {
            throw new TransicionEstadoInvalidaException(
                    "Transición de estado no permitida: " + caso.getEstado() + " -> " + request.estado());
        }

        Optional.ofNullable(request.titulo()).ifPresent(v -> caso.setTitulo(v.trim()));
        Optional.ofNullable(request.descripcion()).ifPresent(caso::setDescripcion);
        Optional.ofNullable(request.modulo()).ifPresent(v -> caso.setModulo(v.trim()));
        caso.setCriticidad(request.criticidad());
        caso.setEstado(request.estado());

        BigDecimal nuevoScore = prioridadService.calcularScore(caso.getCriticidad(), caso.getContadorFallos());
        caso.setScorePrioridad(nuevoScore);

        return mapear(casoPruebaRepository.save(caso));
    }

    @Override
    @Transactional(readOnly = true)
    public CasoPruebaResponse obtener(Long id) {
        return casoPruebaRepository.findById(id)
                .map(this::mapear)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));
    }

    private CasoPruebaResponse mapear(CasoPrueba caso) {
        return new CasoPruebaResponse(
                caso.getId(),
                caso.getTitulo(),
                caso.getDescripcion(),
                caso.getModulo(),
                caso.getCriticidad(),
                caso.getEstado(),
                caso.getScorePrioridad(),
                caso.getRequisito().getId(),
                caso.getFechaActualizacion()
        );
    }
}

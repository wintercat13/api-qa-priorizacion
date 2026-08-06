package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.RequisitoRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.CasoPruebaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CasoPruebaServiceImpl implements CasoPruebaService {

    private final CasoPruebaRepository casoPruebaRepository;
    private final RequisitoRepository requisitoRepository;
    private final UsuarioRepository usuarioRepository;

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

    private CasoPruebaResponse mapear(CasoPrueba caso) {
        return new CasoPruebaResponse(
                caso.getId(),
                caso.getTitulo(),
                caso.getDescripcion(),
                caso.getModulo(),
                caso.getCriticidad(),
                caso.getEstado(),
                caso.getScorePrioridad(),
                caso.getRequisito().getId()
        );
    }
}

package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.RequisitoResponse;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.repository.RequisitoRepository;
import com.qapriorizacion.api.service.RequisitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequisitoServiceImpl implements RequisitoService {

    private final RequisitoRepository requisitoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RequisitoResponse> listar() {
        return requisitoRepository.findAll().stream()
                .map(this::mapear)
                .toList();
    }

    private RequisitoResponse mapear(Requisito requisito) {
        return new RequisitoResponse(
                requisito.getId(),
                requisito.getCodigo(),
                requisito.getNombre(),
                requisito.getDescripcion()
        );
    }
}

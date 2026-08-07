package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.RequisitoResponse;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.repository.RequisitoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequisitoServiceImplTest {

    @Mock
    private RequisitoRepository requisitoRepository;

    @InjectMocks
    private RequisitoServiceImpl requisitoService;

    @Test
    void listar_deberiaRetornarTodosLosRequisitos() {
        Requisito r1 = Requisito.builder().id(1L).codigo("RF-01").nombre("Requisito 1").build();
        Requisito r2 = Requisito.builder().id(2L).codigo("RF-02").nombre("Requisito 2").build();

        when(requisitoRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RequisitoResponse> response = requisitoService.listar();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).codigo()).isEqualTo("RF-01");
        assertThat(response.get(1).codigo()).isEqualTo("RF-02");
    }

    @Test
    void listar_deberiaRetornarListaVacia_cuandoNoHayRequisitos() {
        when(requisitoRepository.findAll()).thenReturn(List.of());

        List<RequisitoResponse> response = requisitoService.listar();

        assertThat(response).isEmpty();
    }
}

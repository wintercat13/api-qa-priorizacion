package com.qapriorizacion.api.job;

import com.qapriorizacion.api.service.CasoPruebaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasoPruebaObsolescenciaJobTest {

    @Mock
    private CasoPruebaService casoPruebaService;

    @InjectMocks
    private CasoPruebaObsolescenciaJob job;

    @Test
    void revisarCasosObsoletos_deberiaInvocarServicioSinExcepciones() {
        when(casoPruebaService.ejecutarRevisionObsolescencia()).thenReturn(3);

        assertThatNoException().isThrownBy(job::revisarCasosObsoletos);

        verify(casoPruebaService).ejecutarRevisionObsolescencia();
    }
}

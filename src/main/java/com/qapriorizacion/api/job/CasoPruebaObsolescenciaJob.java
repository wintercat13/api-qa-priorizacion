package com.qapriorizacion.api.job;

import com.qapriorizacion.api.service.CasoPruebaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CasoPruebaObsolescenciaJob {

    private final CasoPruebaService casoPruebaService;

    @Scheduled(cron = "${app.obsolescencia.cron:0 0 0 * * *}")
    public void revisarCasosObsoletos() {
        log.info("Iniciando revisión programada de casos de prueba obsoletos");
        int marcados = casoPruebaService.ejecutarRevisionObsolescencia();
        log.info("Revisión finalizada: {} caso(s) marcado(s) como obsoleto(s)", marcados);
    }
}

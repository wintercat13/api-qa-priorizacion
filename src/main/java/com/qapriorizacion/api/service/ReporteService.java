package com.qapriorizacion.api.service;

import java.time.LocalDate;

public interface ReporteService {

    byte[] generarReportePdf(LocalDate desde, LocalDate hasta, Long responsableId, String modulo, String prioridad, String estado);

    String generarNombreArchivo(LocalDate desde, LocalDate hasta);
}

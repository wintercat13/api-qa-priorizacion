package com.qapriorizacion.api.service;

import java.time.LocalDate;

public interface ReporteService {

    byte[] generarReportePdf(LocalDate desde, LocalDate hasta);
}

package com.qapriorizacion.api.service;

import java.math.BigDecimal;

public interface SimilitudService {

    BigDecimal calcularSimilitud(String texto1, String texto2);
}

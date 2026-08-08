package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.service.SimilitudService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

@Service
public class SimilitudServiceImpl implements SimilitudService {

    @Override
    public BigDecimal calcularSimilitud(String texto1, String texto2) {
        String a = normalizar(texto1);
        String b = normalizar(texto2);

        if (a.isEmpty() && b.isEmpty()) {
            return BigDecimal.ONE;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> bigramasA = bigramas(a);
        Set<String> bigramasB = bigramas(b);

        Set<String> interseccion = new HashSet<>(bigramasA);
        interseccion.retainAll(bigramasB);

        Set<String> union = new HashSet<>(bigramasA);
        union.addAll(bigramasB);

        if (union.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double coeficiente = (double) interseccion.size() / union.size();
        return BigDecimal.valueOf(coeficiente).setScale(4, RoundingMode.HALF_UP);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private Set<String> bigramas(String texto) {
        Set<String> bigramas = new HashSet<>();
        for (int i = 0; i < texto.length() - 1; i++) {
            bigramas.add(texto.substring(i, i + 2));
        }
        return bigramas;
    }
}

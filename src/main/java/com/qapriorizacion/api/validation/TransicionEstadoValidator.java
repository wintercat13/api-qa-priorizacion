package com.qapriorizacion.api.validation;

import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TransicionEstadoValidator {

    private static final Map<EstadoCasoPrueba, Set<EstadoCasoPrueba>> TRANSICIONES_PERMITIDAS = Map.of(
            EstadoCasoPrueba.PENDIENTE, Set.of(EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.OBSOLETO),
            EstadoCasoPrueba.EN_CURSO, Set.of(EstadoCasoPrueba.EJECUTADO, EstadoCasoPrueba.BLOQUEADO, EstadoCasoPrueba.OBSOLETO),
            EstadoCasoPrueba.BLOQUEADO, Set.of(EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.EJECUTADO, EstadoCasoPrueba.OBSOLETO),
            EstadoCasoPrueba.EJECUTADO, Set.of(EstadoCasoPrueba.OBSOLETO),
            EstadoCasoPrueba.OBSOLETO, Set.of(EstadoCasoPrueba.PENDIENTE, EstadoCasoPrueba.EN_CURSO, EstadoCasoPrueba.ARCHIVADO),
            EstadoCasoPrueba.ARCHIVADO, Set.of()
    );

    public boolean esValida(EstadoCasoPrueba actual, EstadoCasoPrueba nuevo) {
        if (actual == nuevo) {
            return true;
        }
        Set<EstadoCasoPrueba> permitidos = TRANSICIONES_PERMITIDAS.get(actual);
        return permitidos != null && permitidos.contains(nuevo);
    }
}

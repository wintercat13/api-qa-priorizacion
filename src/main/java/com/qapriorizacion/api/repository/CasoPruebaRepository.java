package com.qapriorizacion.api.repository;

import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface CasoPruebaRepository extends JpaRepository<CasoPrueba, Long> {

    List<CasoPrueba> findByModuloIgnoreCaseAndIdNot(String modulo, Long id);

    @Query("""
            SELECT c FROM CasoPrueba c
            WHERE c.estado NOT IN :estadosExcluidos
              AND c.fechaUltimaActividad < :fechaLimite
            """)
    List<CasoPrueba> findCasosObsoletosPotenciales(
            @Param("estadosExcluidos") List<EstadoCasoPrueba> estadosExcluidos,
            @Param("fechaLimite") OffsetDateTime fechaLimite);

    @Modifying
    @Query("""
            UPDATE CasoPrueba c
               SET c.estado = 'OBSOLETO'
             WHERE c.estado NOT IN :estadosExcluidos
               AND c.fechaUltimaActividad < :fechaLimite
            """)
    int marcarCasosObsoletos(
            @Param("estadosExcluidos") List<EstadoCasoPrueba> estadosExcluidos,
            @Param("fechaLimite") OffsetDateTime fechaLimite);
}

package com.qapriorizacion.api.repository;

import com.qapriorizacion.api.entity.Ejecucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EjecucionRepository extends JpaRepository<Ejecucion, Long> {

    Optional<Ejecucion> findTopByCasoPruebaIdOrderByFechaEjecucionDesc(Long casoPruebaId);
}

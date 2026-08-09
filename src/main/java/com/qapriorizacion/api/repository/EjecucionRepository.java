package com.qapriorizacion.api.repository;

import com.qapriorizacion.api.entity.Ejecucion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjecucionRepository extends JpaRepository<Ejecucion, Long> {
}

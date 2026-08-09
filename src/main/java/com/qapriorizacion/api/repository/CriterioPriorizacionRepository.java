package com.qapriorizacion.api.repository;

import com.qapriorizacion.api.entity.CriterioPriorizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CriterioPriorizacionRepository extends JpaRepository<CriterioPriorizacion, Long> {

    List<CriterioPriorizacion> findByActivoTrue();
}

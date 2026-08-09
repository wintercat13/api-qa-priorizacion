package com.qapriorizacion.api.entity;

import com.qapriorizacion.api.entity.enums.ResultadoEjecucion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ejecucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ejecucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caso_prueba_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ejecucion_caso_prueba"))
    private CasoPrueba casoPrueba;

    @ManyToOne
    @JoinColumn(name = "ejecutor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ejecucion_ejecutor"))
    private Usuario ejecutor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ResultadoEjecucion resultado;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_ejecucion", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaEjecucion;
}

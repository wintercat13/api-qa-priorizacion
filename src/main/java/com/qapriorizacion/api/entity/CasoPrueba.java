package com.qapriorizacion.api.entity;

import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "caso_prueba")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CasoPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Criticidad criticidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCasoPrueba estado;

    @Column(name = "score_prioridad", nullable = false, precision = 4, scale = 2)
    private BigDecimal scorePrioridad;

    @Column(name = "posible_duplicado", nullable = false)
    private boolean posibleDuplicado;

    @Column(name = "contador_fallos", nullable = false)
    private int contadorFallos;

    @ManyToOne
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @ManyToOne
    @JoinColumn(name = "requisito_id", nullable = false)
    private Requisito requisito;

    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaActualizacion;

    @Column(name = "fecha_ultima_actividad", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaUltimaActividad;
}

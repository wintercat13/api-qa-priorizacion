-- =====================================================================
-- Sistema Inteligente de Gestión y Priorización de Casos de Prueba
-- V1: esquema base - PostgreSQL 18.x
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. Extensiones
-- ---------------------------------------------------------------------
-- Requerida para comparar similitud de titulo + modulo
-- y detectar posibles casos de prueba duplicados.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ---------------------------------------------------------------------
-- 1. Tabla USUARIO
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id                    SERIAL PRIMARY KEY,
    nombre                VARCHAR(150) NOT NULL,
    correo                VARCHAR(150) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    rol                   VARCHAR(30)  NOT NULL,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    fecha_actualizacion   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('QA_TESTER', 'ADMINISTRADOR_QA', 'DESARROLLADOR'))
);

COMMENT ON TABLE usuario IS 'Usuarios del sistema: QA Tester, Administrador QA, Desarrollador';
COMMENT ON COLUMN usuario.correo IS 'Correo corporativo, dato personal (Ley 19.628)';

-- ---------------------------------------------------------------------
-- 2. Tabla REQUISITO
-- ---------------------------------------------------------------------
CREATE TABLE requisito (
    id              SERIAL PRIMARY KEY,
    codigo          VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    fecha_creacion  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_requisito_codigo UNIQUE (codigo)
);

COMMENT ON TABLE requisito IS 'Requisitos funcionales (RF) validados por casos de prueba';

-- ---------------------------------------------------------------------
-- 3. Tabla CASO_PRUEBA
-- ---------------------------------------------------------------------
CREATE TABLE caso_prueba (
    id                      SERIAL PRIMARY KEY,
    titulo                  VARCHAR(150)  NOT NULL,
    descripcion             VARCHAR(1000),
    modulo                  VARCHAR(100)  NOT NULL,
    criticidad              VARCHAR(10)   NOT NULL,
    estado                  VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    score_prioridad         NUMERIC(4,2)  NOT NULL DEFAULT 0,
    posible_duplicado       BOOLEAN       NOT NULL DEFAULT FALSE,
    caso_similar_id         INT,
    porcentaje_similitud    NUMERIC(5,4),
    contador_fallos         INT           NOT NULL DEFAULT 0,
    responsable_id          INT           NOT NULL,
    requisito_id            INT           NOT NULL,
    fecha_creacion          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    fecha_actualizacion     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    fecha_ultima_actividad  TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_caso_prueba_responsable FOREIGN KEY (responsable_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_caso_prueba_requisito FOREIGN KEY (requisito_id)
        REFERENCES requisito (id) ON DELETE RESTRICT,
    CONSTRAINT fk_caso_prueba_similar FOREIGN KEY (caso_similar_id)
        REFERENCES caso_prueba (id) ON DELETE SET NULL,

    CONSTRAINT ck_caso_prueba_criticidad CHECK (criticidad IN ('ALTA', 'MEDIA', 'BAJA')),
    CONSTRAINT ck_caso_prueba_estado CHECK (
        estado IN ('PENDIENTE', 'EN_CURSO', 'EJECUTADO', 'BLOQUEADO', 'OBSOLETO', 'ARCHIVADO')
    ),
    CONSTRAINT ck_caso_prueba_score CHECK (score_prioridad BETWEEN 0 AND 10),
    CONSTRAINT ck_caso_prueba_similitud CHECK (
        porcentaje_similitud IS NULL OR porcentaje_similitud BETWEEN 0 AND 1
    ),
    CONSTRAINT ck_caso_prueba_contador_fallos CHECK (contador_fallos >= 0),
    CONSTRAINT ck_caso_prueba_no_autoduplicado CHECK (caso_similar_id IS NULL OR caso_similar_id <> id)
);

COMMENT ON TABLE caso_prueba IS 'Casos de prueba';
COMMENT ON COLUMN caso_prueba.caso_similar_id IS 'Caso existente detectado como similar';
COMMENT ON COLUMN caso_prueba.fecha_ultima_actividad IS 'Última edición o ejecución; usada por el job de obsolescencia';
COMMENT ON COLUMN caso_prueba.contador_fallos IS 'Cantidad de ejecuciones con resultado FALLIDO';

-- ---------------------------------------------------------------------
-- 4. Tabla EJECUCION
-- ---------------------------------------------------------------------
CREATE TABLE ejecucion (
    id               SERIAL PRIMARY KEY,
    caso_prueba_id   INT          NOT NULL,
    ejecutor_id      INT          NOT NULL,
    resultado        VARCHAR(15)  NOT NULL,
    observaciones    VARCHAR(500),
    fecha_ejecucion  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_ejecucion_caso_prueba FOREIGN KEY (caso_prueba_id)
        REFERENCES caso_prueba (id) ON DELETE CASCADE,
    CONSTRAINT fk_ejecucion_ejecutor FOREIGN KEY (ejecutor_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,

    CONSTRAINT ck_ejecucion_resultado CHECK (resultado IN ('APROBADO', 'FALLIDO', 'BLOQUEADO'))
);

COMMENT ON TABLE ejecucion IS 'Resultados de ejecución de casos de prueba';

-- ---------------------------------------------------------------------
-- 5. Tabla CRITERIO_PRIORIZACION
-- ---------------------------------------------------------------------
CREATE TABLE criterio_priorizacion (
    id                    SERIAL PRIMARY KEY,
    nombre                VARCHAR(100)  NOT NULL,
    descripcion           VARCHAR(300),
    peso                  NUMERIC(5,4)  NOT NULL,
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_actualizacion   TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_criterio_nombre UNIQUE (nombre),
    CONSTRAINT ck_criterio_peso CHECK (peso >= 0 AND peso <= 1)
);

COMMENT ON TABLE criterio_priorizacion IS 'Criterios y pesos del motor de priorización';

-- ---------------------------------------------------------------------
-- 6. Tabla CASO_PRUEBA_CRITERIO (tabla puente histórica / auditable)
-- ---------------------------------------------------------------------
CREATE TABLE caso_prueba_criterio (
    id               SERIAL PRIMARY KEY,
    caso_prueba_id   INT           NOT NULL,
    criterio_id      INT           NOT NULL,
    valor            NUMERIC(6,3)  NOT NULL,
    peso_aplicado    NUMERIC(5,4)  NOT NULL,
    fecha_calculo    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_cpc_caso_prueba FOREIGN KEY (caso_prueba_id)
        REFERENCES caso_prueba (id) ON DELETE CASCADE,
    CONSTRAINT fk_cpc_criterio FOREIGN KEY (criterio_id)
        REFERENCES criterio_priorizacion (id) ON DELETE RESTRICT,

    CONSTRAINT ck_cpc_peso_aplicado CHECK (peso_aplicado >= 0 AND peso_aplicado <= 1)
);

COMMENT ON TABLE caso_prueba_criterio IS
    'Detalle histórico y auditable de cómo se calculó score_prioridad en cada momento. No se actualiza in-place: cada recálculo inserta filas nuevas.';

-- =====================================================================
-- 7. Índices
-- =====================================================================

-- Búsquedas / filtros frecuentes sobre CASO_PRUEBA
CREATE INDEX idx_caso_prueba_estado ON caso_prueba (estado);
CREATE INDEX idx_caso_prueba_modulo ON caso_prueba (modulo);
CREATE INDEX idx_caso_prueba_requisito ON caso_prueba (requisito_id);
CREATE INDEX idx_caso_prueba_responsable ON caso_prueba (responsable_id);
CREATE INDEX idx_caso_prueba_fecha_ultima_actividad ON caso_prueba (fecha_ultima_actividad);

-- Cola de ejecución priorizada:
-- solo Pendiente, En curso y Bloqueado participan de la cola activa.
CREATE INDEX idx_caso_prueba_cola_priorizada ON caso_prueba (score_prioridad DESC)
    WHERE estado IN ('PENDIENTE', 'EN_CURSO', 'BLOQUEADO');

-- Detección de duplicidad por similitud de texto
CREATE INDEX idx_caso_prueba_titulo_trgm ON caso_prueba USING gin (titulo gin_trgm_ops);
CREATE INDEX idx_caso_prueba_modulo_trgm ON caso_prueba USING gin (modulo gin_trgm_ops);

-- EJECUCION
CREATE INDEX idx_ejecucion_caso_prueba ON ejecucion (caso_prueba_id);
CREATE INDEX idx_ejecucion_ejecutor ON ejecucion (ejecutor_id);
CREATE INDEX idx_ejecucion_fecha ON ejecucion (fecha_ejecucion);

-- CASO_PRUEBA_CRITERIO
CREATE INDEX idx_cpc_caso_prueba ON caso_prueba_criterio (caso_prueba_id, fecha_calculo DESC);
CREATE INDEX idx_cpc_criterio ON caso_prueba_criterio (criterio_id);

-- =====================================================================
-- 8. Funciones y triggers
-- =====================================================================

-- 8.1 Mantiene fecha_actualizacion vigente ante cualquier UPDATE
CREATE OR REPLACE FUNCTION fn_set_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuario_set_fecha_actualizacion
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_actualizacion();

CREATE TRIGGER trg_caso_prueba_set_fecha_actualizacion
    BEFORE UPDATE ON caso_prueba
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_actualizacion();

CREATE TRIGGER trg_criterio_set_fecha_actualizacion
    BEFORE UPDATE ON criterio_priorizacion
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_actualizacion();

-- 8.2 Actualiza fecha_ultima_actividad ante cada edición del caso
CREATE OR REPLACE FUNCTION fn_marcar_actividad_caso()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_ultima_actividad := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_caso_prueba_marcar_actividad
    BEFORE UPDATE ON caso_prueba
    FOR EACH ROW
    EXECUTE FUNCTION fn_marcar_actividad_caso();

-- 8.3 Al registrar una ejecución: actualiza estado del caso, contador de fallos
--     y fecha_ultima_actividad
CREATE OR REPLACE FUNCTION fn_procesar_ejecucion()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.resultado = 'APROBADO' THEN
        UPDATE caso_prueba
           SET estado = 'EJECUTADO',
               fecha_ultima_actividad = now()
         WHERE id = NEW.caso_prueba_id;
    ELSIF NEW.resultado = 'FALLIDO' THEN
        UPDATE caso_prueba
           SET estado = 'BLOQUEADO',
               contador_fallos = contador_fallos + 1,
               fecha_ultima_actividad = now()
         WHERE id = NEW.caso_prueba_id;
    ELSE -- BLOQUEADO como resultado de ejecución
        UPDATE caso_prueba
           SET fecha_ultima_actividad = now()
         WHERE id = NEW.caso_prueba_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ejecucion_procesar
    AFTER INSERT ON ejecucion
    FOR EACH ROW
    EXECUTE FUNCTION fn_procesar_ejecucion();

-- 8.4 Valida que la suma de pesos de criterios activos sea 100%
--     Refuerzo a nivel de datos; la validación principal debe ocurrir en la API
--     para responder 422 antes de llegar a la base.
CREATE OR REPLACE FUNCTION fn_validar_suma_pesos_criterios()
RETURNS TRIGGER AS $$
DECLARE
    suma_pesos NUMERIC(6,4);
BEGIN
    SELECT COALESCE(SUM(peso), 0) INTO suma_pesos
    FROM criterio_priorizacion
    WHERE activo = TRUE;

    IF suma_pesos > 0 AND ABS(suma_pesos - 1.0) > 0.001 THEN
        RAISE EXCEPTION
            'La suma de los pesos de los criterios activos debe ser 1.0 (100%%). Suma actual: %',
            suma_pesos;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_validar_suma_pesos_criterios
    AFTER INSERT OR UPDATE OR DELETE ON criterio_priorizacion
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE FUNCTION fn_validar_suma_pesos_criterios();

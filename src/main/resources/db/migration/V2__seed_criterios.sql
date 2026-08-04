-- =====================================================================
-- V2: Datos iniciales (seed) - criterios de priorización por defecto
-- =====================================================================
INSERT INTO criterio_priorizacion (nombre, descripcion, peso, activo) VALUES
    ('Criticidad del proceso',        'Criticidad funcional/negocio del caso de prueba', 0.35, TRUE),
    ('Riesgo/impacto financiero',     'Impacto económico o regulatorio asociado',        0.30, TRUE),
    ('Historial de fallos',           'Frecuencia de resultados FALLIDO en ejecuciones',  0.20, TRUE),
    ('Frecuencia de uso',             'Frecuencia de uso del módulo/funcionalidad',       0.15, TRUE);

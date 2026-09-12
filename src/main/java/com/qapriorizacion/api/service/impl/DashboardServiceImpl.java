package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.response.DashboardKpiResponse;
import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.EstadoEjecucionResponse;
import com.qapriorizacion.api.dto.response.SerieResponse;
import com.qapriorizacion.api.dto.response.TendenciaResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Ejecucion;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.EjecucionRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CasoPruebaRepository casoPruebaRepository;
    private final EjecucionRepository ejecucionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(LocalDate desde, LocalDate hasta, Long responsableId) {
        if (responsableId != null) {
            validarResponsable(responsableId);
        }

        List<CasoPrueba> casos = casoPruebaRepository.findAll().stream()
                .filter(c -> c.getEstado() != EstadoCasoPrueba.ARCHIVADO)
                .filter(c -> responsableId == null || c.getResponsable().getId().equals(responsableId))
                .filter(c -> estaEnRango(c.getFechaUltimaActividad(), desde, hasta))
                .toList();

        long totalCasos = casos.size();
        long prioridadAlta = casos.stream()
                .filter(c -> c.getCriticidad() == Criticidad.ALTA)
                .count();
        long ejecutados = casos.stream()
                .filter(c -> c.getEstado() == EstadoCasoPrueba.EJECUTADO)
                .count();
        long duplicadosDetectados = casos.stream()
                .filter(CasoPrueba::isPosibleDuplicado)
                .count();

        int porcentajeEjecutados = calcularPorcentaje(ejecutados, totalCasos);

        DashboardKpiResponse kpis = new DashboardKpiResponse(
                totalCasos,
                prioridadAlta,
                porcentajeEjecutados,
                duplicadosDetectados
        );

        List<SerieResponse> casosPorModulo = calcularCasosPorModulo(casos);
        List<EstadoEjecucionResponse> estadoEjecucion = calcularEstadoEjecucion(casos);

        return new DashboardResponse(kpis, casosPorModulo, estadoEjecucion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TendenciaResponse> obtenerTendencias(LocalDate desde, LocalDate hasta, Long responsableId, String agrupacion) {
        if (responsableId != null) {
            validarResponsable(responsableId);
        }

        LocalDate inicio = desde != null ? desde : LocalDate.now().minusMonths(1);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        PeriodoAgrupacion periodo = PeriodoAgrupacion.from(agrupacion);

        List<Ejecucion> ejecuciones = ejecucionRepository.findAll().stream()
                .filter(e -> responsableId == null || e.getEjecutor().getId().equals(responsableId))
                .filter(e -> {
                    LocalDate fecha = e.getFechaEjecucion().toLocalDate();
                    return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
                })
                .toList();

        Map<String, List<Ejecucion>> agrupadas = ejecuciones.stream()
                .collect(Collectors.groupingBy(e -> periodo.formato.apply(e.getFechaEjecucion().toLocalDate())));

        List<String> periodosEsperados = generarPeriodosEsperados(inicio, fin, periodo);

        return periodosEsperados.stream()
                .map(p -> {
                    List<Ejecucion> lista = agrupadas.getOrDefault(p, List.of());
                    long ejecutados = lista.stream().filter(e -> e.getCasoPrueba().getEstado() == EstadoCasoPrueba.EJECUTADO).count();
                    long pendientes = lista.stream().filter(e -> e.getCasoPrueba().getEstado() == EstadoCasoPrueba.PENDIENTE).count();
                    long bloqueados = lista.stream().filter(e -> e.getCasoPrueba().getEstado() == EstadoCasoPrueba.BLOQUEADO).count();
                    return new TendenciaResponse(p, ejecutados, pendientes, bloqueados);
                })
                .toList();
    }

    private void validarResponsable(Long responsableId) {
        if (!usuarioRepository.existsById(responsableId)) {
            throw new RecursoNoEncontradoException("Responsable no encontrado");
        }
    }

    private boolean estaEnRango(OffsetDateTime fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) {
            return true;
        }
        LocalDate ld = fecha.toLocalDate();
        return (desde == null || !ld.isBefore(desde)) && (hasta == null || !ld.isAfter(hasta));
    }

    private List<SerieResponse> calcularCasosPorModulo(List<CasoPrueba> activos) {
        return activos.stream()
                .collect(Collectors.groupingBy(CasoPrueba::getModulo, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new SerieResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(SerieResponse::nombre))
                .toList();
    }

    private List<EstadoEjecucionResponse> calcularEstadoEjecucion(List<CasoPrueba> activos) {
        Map<EstadoCasoPrueba, Long> conteo = activos.stream()
                .collect(Collectors.groupingBy(CasoPrueba::getEstado, Collectors.counting()));

        long total = activos.size();

        return List.of(
                crearEstadoEjecucion("Ejecutado", conteo.getOrDefault(EstadoCasoPrueba.EJECUTADO, 0L), total),
                crearEstadoEjecucion("Pendiente", conteo.getOrDefault(EstadoCasoPrueba.PENDIENTE, 0L), total),
                crearEstadoEjecucion("Bloqueado", conteo.getOrDefault(EstadoCasoPrueba.BLOQUEADO, 0L), total)
        );
    }

    private EstadoEjecucionResponse crearEstadoEjecucion(String estado, long cantidad, long total) {
        return new EstadoEjecucionResponse(estado, cantidad, calcularPorcentaje(cantidad, total));
    }

    private int calcularPorcentaje(long numerador, long denominador) {
        if (denominador == 0) {
            return 0;
        }
        return (int) Math.round((double) numerador / denominador * 100);
    }

    private List<String> generarPeriodosEsperados(LocalDate inicio, LocalDate fin, PeriodoAgrupacion periodo) {
        return Stream.iterate(inicio, d -> !d.isAfter(fin), d -> periodo.siguiente.apply(d))
                .map(periodo.formato::apply)
                .distinct()
                .toList();
    }

    private enum PeriodoAgrupacion {
        DIA("dia", d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE), d -> d.plusDays(1)),
        SEMANA("semana", d -> {
            int year = d.getYear();
            int week = d.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            return year + "-W" + String.format("%02d", week);
        }, d -> d.plusWeeks(1)),
        MES("mes", d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM")), d -> d.plusMonths(1));

        private final String nombre;
        private final Function<LocalDate, String> formato;
        private final Function<LocalDate, LocalDate> siguiente;

        PeriodoAgrupacion(String nombre, Function<LocalDate, String> formato, Function<LocalDate, LocalDate> siguiente) {
            this.nombre = nombre;
            this.formato = formato;
            this.siguiente = siguiente;
        }

        static PeriodoAgrupacion from(String valor) {
            if (valor == null) {
                return DIA;
            }
            return switch (valor.toLowerCase()) {
                case "semana", "week" -> SEMANA;
                case "mes", "month" -> MES;
                default -> DIA;
            };
        }
    }
}

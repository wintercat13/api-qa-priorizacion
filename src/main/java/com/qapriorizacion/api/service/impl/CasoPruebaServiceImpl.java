package com.qapriorizacion.api.service.impl;

import com.qapriorizacion.api.dto.request.CasoPruebaRequest;
import com.qapriorizacion.api.dto.request.CasoPruebaUpdateRequest;
import com.qapriorizacion.api.dto.request.VerificarDuplicidadRequest;
import com.qapriorizacion.api.dto.response.CasoObsoletoResponse;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.dto.response.DuplicidadResponse;
import com.qapriorizacion.api.entity.CasoPrueba;
import com.qapriorizacion.api.entity.Requisito;
import com.qapriorizacion.api.entity.Usuario;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.exception.RecursoNoEncontradoException;
import com.qapriorizacion.api.exception.TransicionEstadoInvalidaException;
import com.qapriorizacion.api.repository.CasoPruebaRepository;
import com.qapriorizacion.api.repository.RequisitoRepository;
import com.qapriorizacion.api.repository.UsuarioRepository;
import com.qapriorizacion.api.service.CasoPruebaService;
import com.qapriorizacion.api.service.PrioridadService;
import com.qapriorizacion.api.service.SimilitudService;
import com.qapriorizacion.api.validation.TransicionEstadoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CasoPruebaServiceImpl implements CasoPruebaService {

    private static final BigDecimal UMBRAL_SIMILITUD = BigDecimal.valueOf(0.70);
    private static final int MESES_INACTIVIDAD_OBSOLESCENCIA = 6;

    private final CasoPruebaRepository casoPruebaRepository;
    private final RequisitoRepository requisitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransicionEstadoValidator transicionEstadoValidator;
    private final PrioridadService prioridadService;
    private final SimilitudService similitudService;

    @Override
    @Transactional
    public CasoPruebaResponse crear(CasoPruebaRequest request, String correoResponsable) {
        Usuario responsable = usuarioRepository.findByCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario responsable no encontrado"));

        Requisito requisito = requisitoRepository.findById(request.requisitoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Requisito no encontrado"));

        CasoPrueba caso = CasoPrueba.builder()
                .titulo(request.titulo().trim())
                .descripcion(request.descripcion())
                .modulo(request.modulo().trim())
                .criticidad(request.criticidad())
                .estado(EstadoCasoPrueba.PENDIENTE)
                .scorePrioridad(BigDecimal.ZERO)
                .posibleDuplicado(false)
                .contadorFallos(0)
                .responsable(responsable)
                .requisito(requisito)
                .build();

        CasoPrueba guardado = casoPruebaRepository.save(caso);
        evaluarDuplicidad(guardado);
        return mapear(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CasoPruebaResponse> listar() {
        return casoPruebaRepository.findAll().stream()
                .map(this::mapear)
                .toList();
    }

    @Override
    @Transactional
    public CasoPruebaResponse actualizar(Long id, CasoPruebaUpdateRequest request) {
        CasoPrueba caso = casoPruebaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));

        if (!transicionEstadoValidator.esValida(caso.getEstado(), request.estado())) {
            throw new TransicionEstadoInvalidaException(
                    "Transición de estado no permitida: " + caso.getEstado() + " -> " + request.estado());
        }

        Optional.ofNullable(request.titulo()).ifPresent(v -> caso.setTitulo(v.trim()));
        Optional.ofNullable(request.descripcion()).ifPresent(caso::setDescripcion);
        Optional.ofNullable(request.modulo()).ifPresent(v -> caso.setModulo(v.trim()));
        caso.setCriticidad(request.criticidad());
        caso.setEstado(request.estado());

        BigDecimal nuevoScore = prioridadService.calcularScore(caso.getCriticidad(), caso.getContadorFallos());
        caso.setScorePrioridad(nuevoScore);

        CasoPrueba actualizado = casoPruebaRepository.save(caso);
        evaluarDuplicidad(actualizado);

        return mapear(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public CasoPruebaResponse obtener(Long id) {
        return casoPruebaRepository.findById(id)
                .map(this::mapear)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public DuplicidadResponse verificarDuplicidad(VerificarDuplicidadRequest request) {
        return buscarMejorCoincidencia(request.titulo(), request.modulo(), null)
                .map(r -> new DuplicidadResponse(true, r.caso.getId(), r.similitud))
                .orElse(new DuplicidadResponse(false, null, null));
    }

    @Override
    @Transactional
    public CasoPruebaResponse confirmarNoDuplicado(Long id) {
        CasoPrueba caso = casoPruebaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));

        caso.setPosibleDuplicado(false);
        caso.setCasoSimilarId(null);
        caso.setPorcentajeSimilitud(null);

        return mapear(casoPruebaRepository.save(caso));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CasoObsoletoResponse> listarObsoletos() {
        OffsetDateTime fechaLimite = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(MESES_INACTIVIDAD_OBSOLESCENCIA);
        List<EstadoCasoPrueba> estadosExcluidos = List.of(EstadoCasoPrueba.OBSOLETO, EstadoCasoPrueba.ARCHIVADO);

        return casoPruebaRepository.findCasosObsoletosPotenciales(estadosExcluidos, fechaLimite).stream()
                .map(c -> new CasoObsoletoResponse(
                        c.getId(),
                        c.getTitulo(),
                        c.getFechaUltimaActividad().toLocalDate()))
                .toList();
    }

    @Override
    @Transactional
    public CasoPruebaResponse archivar(Long id) {
        CasoPrueba caso = casoPruebaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso de prueba no encontrado"));

        if (!transicionEstadoValidator.esValida(caso.getEstado(), EstadoCasoPrueba.ARCHIVADO)) {
            throw new TransicionEstadoInvalidaException(
                    "Transición de estado no permitida: " + caso.getEstado() + " -> ARCHIVADO");
        }

        caso.setEstado(EstadoCasoPrueba.ARCHIVADO);
        return mapear(casoPruebaRepository.save(caso));
    }

    @Override
    @Transactional
    public int ejecutarRevisionObsolescencia() {
        OffsetDateTime fechaLimite = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(MESES_INACTIVIDAD_OBSOLESCENCIA);
        List<EstadoCasoPrueba> estadosExcluidos = List.of(EstadoCasoPrueba.OBSOLETO, EstadoCasoPrueba.ARCHIVADO);
        return casoPruebaRepository.marcarCasosObsoletos(estadosExcluidos, fechaLimite);
    }

    private void evaluarDuplicidad(CasoPrueba caso) {
        buscarMejorCoincidencia(caso.getTitulo(), caso.getModulo(), caso.getId())
                .ifPresentOrElse(resultado -> {
                    caso.setPosibleDuplicado(true);
                    caso.setCasoSimilarId(resultado.caso().getId());
                    caso.setPorcentajeSimilitud(resultado.similitud());
                }, () -> {
                    caso.setPosibleDuplicado(false);
                    caso.setCasoSimilarId(null);
                    caso.setPorcentajeSimilitud(null);
                });
    }

    private Optional<ResultadoSimilitud> buscarMejorCoincidencia(String titulo, String modulo, Long idExcluir) {
        return casoPruebaRepository.findByModuloIgnoreCaseAndIdNot(modulo, idExcluir != null ? idExcluir : -1L).stream()
                .filter(c -> c.getEstado() != EstadoCasoPrueba.ARCHIVADO)
                .map(c -> {
                    BigDecimal simTitulo = similitudService.calcularSimilitud(titulo, c.getTitulo());
                    BigDecimal simModulo = similitudService.calcularSimilitud(modulo, c.getModulo());
                    BigDecimal combinada = simTitulo.multiply(BigDecimal.valueOf(0.8))
                            .add(simModulo.multiply(BigDecimal.valueOf(0.2)));
                    return new ResultadoSimilitud(c, combinada);
                })
                .filter(r -> r.similitud().compareTo(UMBRAL_SIMILITUD) >= 0)
                .max(Comparator.comparing(ResultadoSimilitud::similitud));
    }

    private record ResultadoSimilitud(CasoPrueba caso, BigDecimal similitud) {
    }

    private CasoPruebaResponse mapear(CasoPrueba caso) {
        return new CasoPruebaResponse(
                caso.getId(),
                caso.getTitulo(),
                caso.getDescripcion(),
                caso.getModulo(),
                caso.getCriticidad(),
                caso.getEstado(),
                caso.getScorePrioridad(),
                caso.isPosibleDuplicado(),
                caso.getCasoSimilarId(),
                caso.getPorcentajeSimilitud(),
                caso.getRequisito().getId(),
                caso.getFechaActualizacion(),
                caso.getFechaUltimaActividad()
        );
    }
}

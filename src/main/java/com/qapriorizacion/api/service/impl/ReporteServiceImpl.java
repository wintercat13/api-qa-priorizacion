package com.qapriorizacion.api.service.impl;

import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.qapriorizacion.api.dto.response.CasoPruebaResponse;
import com.qapriorizacion.api.dto.response.DashboardResponse;
import com.qapriorizacion.api.dto.response.EstadoEjecucionResponse;
import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.dto.response.SerieResponse;
import com.qapriorizacion.api.dto.response.TendenciaResponse;
import com.qapriorizacion.api.entity.enums.Criticidad;
import com.qapriorizacion.api.entity.enums.EstadoCasoPrueba;
import com.qapriorizacion.api.exception.SinDatosReporteException;
import com.qapriorizacion.api.service.CasoPruebaService;
import com.qapriorizacion.api.service.DashboardService;
import com.qapriorizacion.api.service.MetricasService;
import com.qapriorizacion.api.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final Color COLOR_EJECUTADO = new Color(0x0F8B8D);
    private static final Color COLOR_PENDIENTE = new Color(0xD98A2B);
    private static final Color COLOR_BLOQUEADO = new Color(0xB5473D);
    private static final Color COLOR_FONDO_BARRA = new Color(0xE9ECEF);
    private static final Color COLOR_TEXTO = new Color(0x212529);
    private static final Color COLOR_SUBTITULO = new Color(0x6C757D);

    private static final int ANCHO_GRAFICO = 620;
    private static final int ANCHO_GRAFICO_DONA = 340;
    private static final int ALTO_GRAFICO_LINEAS = 320;
    private static final int ALTO_GRAFICO_BARRAS = 280;
    private static final int ALTO_GRAFICO_DONA = 300;

    private final MetricasService metricasService;
    private final DashboardService dashboardService;
    private final CasoPruebaService casoPruebaService;

    @Override
    public byte[] generarReportePdf(LocalDate desde, LocalDate hasta, Long responsableId,
                                    String modulo, String prioridad, String estado) {
        MetricasResponse metricas = metricasService.calcularMetricas(desde, hasta);

        if (metricas.cobertura() == 0 && metricas.porcentajeEjecutado() == 0) {
            throw new SinDatosReporteException("No hay datos suficientes para generar el reporte en el período seleccionado");
        }

        DashboardResponse dashboard = dashboardService.obtenerDashboard(desde, hasta, responsableId);
        List<TendenciaResponse> tendencias = dashboardService.obtenerTendencias(desde, hasta, responsableId, "semana");

        List<CasoPruebaResponse> casos = filtrarCasos(casoPruebaService.listar(), modulo, prioridad, estado);

        String html = construirHtml(desde, hasta, responsableId, modulo, prioridad, estado,
                metricas, tendencias, dashboard, casos);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            try (PdfBoxRenderer renderer = builder.buildPdfRenderer()) {
                renderer.createPDF();
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte PDF", e);
        }
    }

    @Override
    public String generarNombreArchivo(LocalDate desde, LocalDate hasta) {
        return "reporte_qa_" + formatearNombre(desde) + "_" + formatearNombre(hasta) + ".pdf";
    }

    private String construirHtml(LocalDate desde, LocalDate hasta, Long responsableId,
                                 String modulo, String prioridad, String estado,
                                 MetricasResponse metricas,
                                 List<TendenciaResponse> tendencias, DashboardResponse dashboard,
                                 List<CasoPruebaResponse> casos) {
        String plantilla = cargarPlantilla();

        String filtrosHtml = construirFiltrosHtml(responsableId, modulo, prioridad, estado);
        String filasCasos = casos.stream()
                .map(this::filaCasoHtml)
                .collect(Collectors.joining());

        return plantilla
                .replace("${periodo}", formatear(desde) + " - " + formatear(hasta))
                .replace("${filtros}", filtrosHtml)
                .replace("${cobertura}", String.valueOf(metricas.cobertura()))
                .replace("${porcentajeEjecutado}", String.valueOf(metricas.porcentajeEjecutado()))
                .replace("${casosObsoletosDepurados}", String.valueOf(metricas.casosObsoletosDepurados()))
                .replace("${cumplimientoSLA}", String.valueOf(metricas.cumplimientoSLA()))
                .replace("${tendenciasChart}", imagenBase64(graficoTendencias(tendencias)))
                .replace("${modulosChart}", imagenBase64(graficoCasosPorModulo(dashboard.casosPorModulo())))
                .replace("${estadoChart}", imagenBase64(graficoEstadoEjecucion(dashboard.estadoEjecucion())))
                .replace("${cantidadCasos}", String.valueOf(casos.size()))
                .replace("${filasCasos}", filasCasos);
    }

    private String cargarPlantilla() {
        try {
            return new String(new ClassPathResource("reportes/reporte-qa.html").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la plantilla del reporte", e);
        }
    }

    private String construirFiltrosHtml(Long responsableId, String modulo, String prioridad, String estado) {
        StringBuilder sb = new StringBuilder();
        if (!estaVacio(modulo)) {
            sb.append(filaFiltro("Módulo", modulo));
        }
        if (!estaVacio(prioridad)) {
            sb.append(filaFiltro("Prioridad", prioridad));
        }
        if (!estaVacio(estado)) {
            sb.append(filaFiltro("Estado", estado));
        }
        if (responsableId != null) {
            sb.append(filaFiltro("Responsable", "ID " + responsableId));
        }
        return sb.toString();
    }

    private String filaFiltro(String label, String valor) {
        return "<tr><td class=\"label\">" + label + ":</td><td>" + escapeHtml(valor) + "</td></tr>";
    }

    private String filaCasoHtml(CasoPruebaResponse c) {
        return """
                <tr>
                    <td>#%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td><span class=\"badge badge-prioridad-%s\">%s</span></td>
                    <td><span class=\"badge badge-estado-%s\">%s</span></td>
                    <td>%s</td>
                </tr>
                """.formatted(
                c.id(),
                escapeHtml(c.titulo()),
                escapeHtml(c.modulo()),
                c.criticidad().name().toLowerCase(), c.criticidad().name(),
                c.estado().name().toLowerCase().replace("_", "-"), c.estado().name(),
                c.scorePrioridad() != null ? c.scorePrioridad().toString() : "-"
        );
    }

    private String escapeHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String imagenBase64(BufferedImage imagen) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al codificar imagen del gráfico", e);
        }
    }

    private BufferedImage graficoTendencias(List<TendenciaResponse> tendencias) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (TendenciaResponse t : tendencias) {
            dataset.addValue(t.ejecutados(), "Ejecutados", t.periodo());
            dataset.addValue(t.pendientes(), "Pendientes", t.periodo());
            dataset.addValue(t.bloqueados(), "Bloqueados", t.periodo());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                null, "", "", dataset,
                PlotOrientation.VERTICAL, true, false, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(0xDEE2E6));
        plot.setDomainGridlinesVisible(false);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, COLOR_EJECUTADO);
        renderer.setSeriesPaint(1, COLOR_PENDIENTE);
        renderer.setSeriesPaint(2, COLOR_BLOQUEADO);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesStroke(2, new BasicStroke(2.5f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
        renderer.setSeriesShape(1, new Ellipse2D.Double(-3, -3, 6, 6));
        renderer.setSeriesShape(2, new Ellipse2D.Double(-3, -3, 6, 6));
        plot.setRenderer(renderer);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));

        BufferedImage imagen = new BufferedImage(ANCHO_GRAFICO, ALTO_GRAFICO_LINEAS, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = imagen.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        chart.draw(g2, new java.awt.geom.Rectangle2D.Double(0, 0, ANCHO_GRAFICO, ALTO_GRAFICO_LINEAS));
        g2.dispose();
        return imagen;
    }

    private BufferedImage graficoCasosPorModulo(List<SerieResponse> casosPorModulo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SerieResponse s : casosPorModulo) {
            dataset.addValue(s.valor(), "Casos", s.nombre());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null, null, null, dataset,
                PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setAxisOffset(new org.jfree.chart.ui.RectangleInsets(0, 0, 0, 0));

        BarRenderer renderer = new BarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, COLOR_EJECUTADO);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(new Color(0xFFFFFF));
        renderer.setPositiveItemLabelPositionFallback(new org.jfree.chart.labels.ItemLabelPosition(
                org.jfree.chart.labels.ItemLabelAnchor.INSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.45);
        renderer.setItemMargin(0.15);
        plot.setRenderer(renderer);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setMaximumCategoryLabelWidthRatio(2.0f);
        domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
        domainAxis.setTickLabelPaint(new Color(0x495057));
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);
        domainAxis.setCategoryMargin(0.20);

        org.jfree.chart.axis.ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setVisible(false);

        BufferedImage imagen = new BufferedImage(ANCHO_GRAFICO, ALTO_GRAFICO_BARRAS, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = imagen.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        chart.draw(g2, new java.awt.geom.Rectangle2D.Double(0, 0, ANCHO_GRAFICO, ALTO_GRAFICO_BARRAS));
        g2.dispose();
        return imagen;
    }

    private BufferedImage graficoEstadoEjecucion(List<EstadoEjecucionResponse> estadoEjecucion) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (EstadoEjecucionResponse e : estadoEjecucion) {
            if (e.cantidad() > 0) {
                dataset.setValue(e.estado(), e.cantidad());
            }
        }

        JFreeChart chart = ChartFactory.createRingChart(
                null, dataset, true, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setSectionPaint("Ejecutado", COLOR_EJECUTADO);
        plot.setSectionPaint("Pendiente", COLOR_PENDIENTE);
        plot.setSectionPaint("Bloqueado", COLOR_BLOQUEADO);
        plot.setLabelGenerator(null);
        plot.setSimpleLabels(true);
        plot.setSectionOutlinesVisible(false);

        java.awt.Font legendFont = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12);
        chart.getLegend().setItemFont(legendFont);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setBackgroundPaint(Color.WHITE);
        chart.getLegend().setItemLabelPadding(new org.jfree.chart.ui.RectangleInsets(3, 6, 3, 12));

        BufferedImage imagen = new BufferedImage(ANCHO_GRAFICO_DONA, ALTO_GRAFICO_DONA, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = imagen.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        chart.draw(g2, new java.awt.geom.Rectangle2D.Double(0, 0, ANCHO_GRAFICO_DONA, ALTO_GRAFICO_DONA));
        g2.dispose();
        return imagen;
    }

    private List<CasoPruebaResponse> filtrarCasos(List<CasoPruebaResponse> casos, String modulo, String prioridad, String estado) {
        Predicate<CasoPruebaResponse> filtro = c -> true;
        if (!estaVacio(modulo)) {
            filtro = filtro.and(c -> c.modulo().equalsIgnoreCase(modulo.trim()));
        }
        if (!estaVacio(prioridad)) {
            Criticidad criticidad = parsearCriticidad(prioridad);
            filtro = filtro.and(c -> c.criticidad() == criticidad);
        }
        if (!estaVacio(estado)) {
            EstadoCasoPrueba estadoEnum = parsearEstado(estado);
            filtro = filtro.and(c -> c.estado() == estadoEnum);
        }
        return casos.stream()
                .filter(filtro)
                .sorted(Comparator.comparing(CasoPruebaResponse::id))
                .toList();
    }

    private Criticidad parsearCriticidad(String prioridad) {
        return Criticidad.valueOf(prioridad.trim().toUpperCase());
    }

    private EstadoCasoPrueba parsearEstado(String estado) {
        return EstadoCasoPrueba.valueOf(estado.trim().toUpperCase());
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String formatear(LocalDate fecha) {
        if (fecha == null) {
            return "No especificada";
        }
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatearNombre(LocalDate fecha) {
        if (fecha == null) {
            return "sin_fecha";
        }
        return fecha.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

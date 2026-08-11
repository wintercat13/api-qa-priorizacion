package com.qapriorizacion.api.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.qapriorizacion.api.dto.response.MetricasResponse;
import com.qapriorizacion.api.exception.SinDatosReporteException;
import com.qapriorizacion.api.service.MetricasService;
import com.qapriorizacion.api.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final MetricasService metricasService;

    @Override
    public byte[] generarReportePdf(LocalDate desde, LocalDate hasta) {
        MetricasResponse metricas = metricasService.calcularMetricas(desde, hasta);

        if (metricas.cobertura() == 0 && metricas.porcentajeEjecutado() == 0) {
            throw new SinDatosReporteException("No hay datos suficientes para generar el reporte en el período seleccionado");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Reporte Semanal de QA", tituloFont));
            document.add(new Paragraph(" "));

            String periodo = "Período: " + formatear(desde) + " - " + formatear(hasta);
            document.add(new Paragraph(periodo, subtituloFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Métricas principales", subtituloFont));
            document.add(new Paragraph("Cobertura: " + metricas.cobertura() + "%", normalFont));
            document.add(new Paragraph("Porcentaje ejecutado: " + metricas.porcentajeEjecutado() + "%", normalFont));
            document.add(new Paragraph("Casos obsoletos depurados: " + metricas.casosObsoletosDepurados(), normalFont));
            document.add(new Paragraph("Cumplimiento SLA: " + metricas.cumplimientoSLA() + "%", normalFont));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar el reporte PDF", e);
        }
    }

    private String formatear(LocalDate fecha) {
        if (fecha == null) {
            return "No especificada";
        }
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}

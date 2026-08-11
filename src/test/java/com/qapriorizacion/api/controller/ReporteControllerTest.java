package com.qapriorizacion.api.controller;

import com.qapriorizacion.api.security.JwtAuthFilter;
import com.qapriorizacion.api.security.JwtService;
import com.qapriorizacion.api.security.UserDetailsServiceImpl;
import com.qapriorizacion.api.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReporteController.class)
@AutoConfigureMockMvc
@Import({com.qapriorizacion.api.config.SecurityConfig.class, JwtAuthFilter.class})
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR_QA")
    void exportar_deberiaRetornar200ConPdf_cuandoFormatoEsPdf() throws Exception {
        byte[] pdf = "%PDF-1.4\n".getBytes();
        when(reporteService.generarReportePdf(null, null)).thenReturn(pdf);

        mockMvc.perform(get("/api/v1/reportes/export")
                        .param("formato", "pdf")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"reporte_qa_semana.pdf\""));
    }

    @Test
    @WithMockUser(roles = "QA_TESTER")
    void exportar_deberiaRetornar403_cuandoRolNoEsAdministradorQA() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/export")
                        .param("formato", "pdf")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

package uce.edu.MiPedido.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uce.edu.MiPedido.Service.ReporteService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@Controller
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/reportes")
    public String paginaReportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (inicio == null) {
            inicio = LocalDate.now();
        }
        if (fin == null) {
            fin = LocalDate.now();
        }

        Map<String, Object> datos = reporteService.obtenerDatosReporte(inicio, fin);
        model.addAllAttributes(datos);
        model.addAttribute("fechaInicio", inicio);
        model.addAttribute("fechaFin", fin);

        return "reportes";
    }

    // --- EXPORTAR PDF CON FECHAS ---
    @GetMapping("/reportes/exportar/pdf")
    public void exportarPDF(
            HttpServletResponse response,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) throws IOException {

        // Si no llegan fechas, usar HOY por defecto
        if (inicio == null) {
            inicio = LocalDate.now();
        }
        if (fin == null) {
            fin = LocalDate.now();
        }

        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Reporte_" + inicio + "_al_" + fin + ".pdf";
        response.setHeader(headerKey, headerValue);

        // Llamar al servicio pasando las fechas
        reporteService.exportarReporteVentasPDF(response, inicio, fin);
    }
}

package uce.edu.MiPedido.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import uce.edu.MiPedido.Service.ReporteService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/reportes/exportar/pdf")
    public void exportarPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ventas_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        reporteService.exportarReporteVentasPDF(response);
    }
    
    @GetMapping("/reportes")
    public String paginaReportes() {
        return "reportes"; // Crear vista simple con botón de descarga
    }
}
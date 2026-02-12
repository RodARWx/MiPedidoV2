package uce.edu.MiPedido.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Repository.PedidoRepository;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public void exportarReporteVentasPDF(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Título
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        Paragraph paragraph = new Paragraph("Reporte de Ventas y Platos Populares", fontTitle);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);
        document.add(new Paragraph(" "));

        // Tabla de Platos Más Vendidos
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);
        table.addCell(new Phrase("Producto", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        table.addCell(new Phrase("Cantidad Vendida", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

        List<Object[]> platosMasVendidos = pedidoRepository.obtenerPlatosMasVendidos();

        for (Object[] fila : platosMasVendidos) {
            table.addCell(fila[0].toString());
            table.addCell(fila[1].toString());
        }

        document.add(new Paragraph("Ranking de Productos (Más Vendidos):"));
        document.add(new Paragraph(" "));
        document.add(table);

        document.close();
    }
}

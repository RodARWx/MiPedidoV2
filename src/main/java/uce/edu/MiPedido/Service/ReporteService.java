package uce.edu.MiPedido.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Caja;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Pedido;
import uce.edu.MiPedido.Repository.CajaRepository;
import uce.edu.MiPedido.Repository.PedidoRepository;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CajaRepository cajaRepository;

    // Colores corporativos
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private static final Color TABLE_HEADER_COLOR = new Color(30, 30, 45);
    private static final Color TABLE_BODY_COLOR_1 = new Color(255, 255, 255);
    private static final Color TABLE_BODY_COLOR_2 = new Color(243, 244, 246);

    // Método para obtener datos (Vista HTML)
    public Map<String, Object> obtenerDatosReporte(LocalDate inicio, LocalDate fin) {
        LocalDateTime fechaInicio = inicio.atStartOfDay();
        LocalDateTime fechaFin = fin.atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
        double totalVentas = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO)
                .mapToDouble(Pedido::getTotal).sum();

        List<Caja> cajas = cajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
        double totalGastos = cajas.stream().mapToDouble(Caja::getTotalEgresos).sum();

        List<Object[]> tops = pedidoRepository.obtenerPlatosMasVendidosPorFecha(fechaInicio, fechaFin);

        Map<String, Object> datos = new HashMap<>();
        datos.put("pedidos", pedidos);
        datos.put("cajas", cajas);
        datos.put("totalVentas", totalVentas);
        datos.put("totalGastos", totalGastos);
        datos.put("balance", totalVentas - totalGastos);
        datos.put("tops", tops);
        return datos;
    }

    // --- GENERAR PDF DETALLADO ---
    public void exportarReporteVentasPDF(HttpServletResponse response, LocalDate inicio, LocalDate fin) throws IOException {
        // 1. Obtener datos filtrados por fecha
        LocalDateTime fechaInicio = inicio.atStartOfDay();
        LocalDateTime fechaFin = fin.atTime(LocalTime.MAX);

        List<Caja> cajas = cajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
        List<Object[]> tops = pedidoRepository.obtenerPlatosMasVendidosPorFecha(fechaInicio, fechaFin);

        // Calcular totales generales para el encabezado
        double totalVentasPeriodo = 0;
        // Podríamos recalcularlo o pasarlo, aquí lo calculo rápido con las cajas para consistencia
        double totalIngresosCajas = cajas.stream().mapToDouble(Caja::getTotalIngresos).sum();
        double totalGastosCajas = cajas.stream().mapToDouble(Caja::getTotalEgresos).sum();
        double balanceTotal = totalIngresosCajas - totalGastosCajas;

        // 2. Configurar Documento
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // --- TÍTULO Y RANGO ---
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PRIMARY_COLOR);
        Paragraph titulo = new Paragraph("Reporte de Gestión Financiera", fontTitle);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);

        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        String rangoFechas = "Periodo: " + inicio + " al " + fin;
        Paragraph subTitulo = new Paragraph(rangoFechas + "\nGenerado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontSub);
        subTitulo.setAlignment(Paragraph.ALIGN_CENTER);
        subTitulo.setSpacingAfter(20);
        document.add(subTitulo);

        // --- RESUMEN FINANCIERO (CUADRO) ---
        PdfPTable tableResumen = new PdfPTable(3);
        tableResumen.setWidthPercentage(100f);
        tableResumen.setSpacingAfter(20);

        crearCeldaResumen(tableResumen, "INGRESOS TOTALES", "+ $" + totalIngresosCajas, new Color(220, 252, 231), new Color(22, 101, 52));
        crearCeldaResumen(tableResumen, "GASTOS TOTALES", "- $" + totalGastosCajas, new Color(254, 226, 226), new Color(153, 27, 27));
        crearCeldaResumen(tableResumen, "BALANCE NETO", "$" + balanceTotal, new Color(219, 234, 254), new Color(30, 64, 175));

        document.add(tableResumen);

        // --- TABLA 1: HISTORIAL DE CAJAS (LO QUE PEDISTE) ---
        Paragraph titleCajas = new Paragraph("Historial de Cierres de Caja", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TABLE_HEADER_COLOR));
        titleCajas.setSpacingAfter(10);
        document.add(titleCajas);

        PdfPTable tableCajas = new PdfPTable(5); // Fecha, Inicial, Ingresos, Gastos, Estado
        tableCajas.setWidthPercentage(100f);
        tableCajas.setWidths(new float[]{2f, 1.5f, 1.5f, 1.5f, 1.5f});

        crearCeldaEncabezado(tableCajas, "FECHA APERTURA");
        crearCeldaEncabezado(tableCajas, "SALDO INICIAL");
        crearCeldaEncabezado(tableCajas, "INGRESOS");
        crearCeldaEncabezado(tableCajas, "GASTOS");
        crearCeldaEncabezado(tableCajas, "ESTADO");

        if (cajas.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No hay registros de caja en este periodo.", fontSub));
            emptyCell.setColspan(5);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(10);
            tableCajas.addCell(emptyCell);
        } else {
            boolean alt = false;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            for (Caja c : cajas) {
                crearCeldaDato(tableCajas, c.getFechaApertura().format(fmt), false, alt);
                crearCeldaDato(tableCajas, "$" + c.getSaldoInicial(), false, alt);
                crearCeldaDato(tableCajas, "$" + c.getTotalIngresos(), false, alt);
                crearCeldaDato(tableCajas, "$" + c.getTotalEgresos(), false, alt);
                crearCeldaDato(tableCajas, c.isAbierta() ? "ABIERTA" : "CERRADA", false, alt);
                alt = !alt;
            }
        }
        document.add(tableCajas);

        // --- TABLA 2: PRODUCTOS TOP ---
        document.add(new Paragraph("\n")); // Espacio
        Paragraph titleProd = new Paragraph("Productos Más Vendidos (Top)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TABLE_HEADER_COLOR));
        titleProd.setSpacingAfter(10);
        document.add(titleProd);

        PdfPTable tableProd = new PdfPTable(2);
        tableProd.setWidthPercentage(100f);
        tableProd.setWidths(new float[]{4f, 1f});

        crearCeldaEncabezado(tableProd, "PRODUCTO");
        crearCeldaEncabezado(tableProd, "CANTIDAD");

        if (tops.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("Sin ventas registradas.", fontSub));
            emptyCell.setColspan(2);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(10);
            tableProd.addCell(emptyCell);
        } else {
            boolean alt = false;
            for (Object[] row : tops) {
                crearCeldaDato(tableProd, row[0].toString(), true, alt);
                crearCeldaDato(tableProd, row[1].toString(), false, alt);
                alt = !alt;
            }
        }
        document.add(tableProd);

        document.close();
    }

    // --- HELPERS DE DISEÑO ---
    private void crearCeldaEncabezado(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(TABLE_HEADER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorderColor(Color.WHITE);
        table.addCell(cell);
    }

    private void crearCeldaDato(PdfPTable table, String text, boolean alignLeft, boolean altColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY)));
        cell.setBackgroundColor(altColor ? TABLE_BODY_COLOR_2 : TABLE_BODY_COLOR_1);
        cell.setHorizontalAlignment(alignLeft ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorderColor(new Color(230, 230, 230));
        table.addCell(cell);
    }

    private void crearCeldaResumen(PdfPTable table, String label, String value, Color bgColor, Color textColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(15);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textColor));
        pLabel.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLabel);

        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, textColor));
        pValue.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pValue);

        table.addCell(cell);
    }
}

package uce.edu.MiPedido.Config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uce.edu.MiPedido.Model.*;
import uce.edu.MiPedido.Repository.*;
import uce.edu.MiPedido.Service.CajaService;
import uce.edu.MiPedido.Service.PedidoService;

@Component
public class MassDataSeeder implements CommandLineRunner {

    @Autowired private MesaRepository mesaRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private CajaRepository cajaRepository;

    @Autowired private PedidoService pedidoService;
    @Autowired private CajaService cajaService;

    private final Random random = new Random();

    // Ajusta si quieres más/menos días (mantiene 1-2 confirmados por día)
    private static final int DAYS = 60;

    private static final int TOTAL_PEDIDOS = 4000;
    private static final int TOTAL_CATEGORIAS = 10;
    private static final int TOTAL_MESAS = 10;
    private static final int TOTAL_PRODUCTOS = 50;

    // 5% cancelado
    private static final int TOTAL_CANCELADOS = (int) (TOTAL_PEDIDOS * 0.05);

    @Override
    @Transactional
    public void run(String... args) {

        // Evitar duplicar (si ya hay pedidos, no siembra)
        if (pedidoRepository.count() > 0) {
            System.out.println("[Seeder] Ya existen pedidos. No se ejecuta el seeder.");
            return;
        }

        System.out.println("[Seeder] Iniciando carga masiva...");

        asegurarMesas(TOTAL_MESAS);
        asegurarCategorias(TOTAL_CATEGORIAS);
        asegurarProductos(TOTAL_PRODUCTOS);

        List<Mesa> mesas = mesaRepository.findAll();
        List<Categoria> categorias = categoriaRepository.findAll();
        List<Producto> productos = productoRepository.findAll();

        // Distribución de pedidos por día: suma EXACTA = 1000
        int basePerDay = TOTAL_PEDIDOS / DAYS;
        int remainder = TOTAL_PEDIDOS % DAYS;

        // Cancelados distribuidos por días
        int[] canceladosPorDia = new int[DAYS];
        for (int i = 0; i < TOTAL_CANCELADOS; i++) {
            canceladosPorDia[random.nextInt(DAYS)]++;
        }

        // Si había una caja abierta previa (raro en BD limpia), ciérrala para no romper "una abierta a la vez"
        Caja abierta = cajaService.obtenerCajaAbierta();
        if (abierta != null) {
            abierta.setAbierta(false);
            abierta.setFechaCierre(LocalDateTime.now());
            cajaRepository.save(abierta);
        }

        LocalDate start = LocalDate.now().minusDays(DAYS - 1);

        int carryCancel = 0;
        int totalGenerados = 0;

        for (int dayIndex = 0; dayIndex < DAYS; dayIndex++) {
            LocalDate dia = start.plusDays(dayIndex);

            // Pedidos del día
            int pedidosHoy = basePerDay + (dayIndex < remainder ? 1 : 0);

            // 1–2 confirmados por día (sin ocupar mesa: PARA_LLEVAR para no bloquear mesas)
            int confirmadosHoy = 1 + random.nextInt(2);
            confirmadosHoy = Math.min(confirmadosHoy, pedidosHoy);

            // Cancelados hoy (con carry por si algún día no alcanzara; normalmente sobra capacidad)
            int cancelWanted = canceladosPorDia[dayIndex] + carryCancel;
            int espacioNoConfirmado = pedidosHoy - confirmadosHoy;

            int canceladosHoy = Math.min(cancelWanted, espacioNoConfirmado);
            carryCancel = cancelWanted - canceladosHoy;

            int pagadosHoy = pedidosHoy - confirmadosHoy - canceladosHoy;

            // Abrir caja del día (abierta=true) para que registrarIngreso funcione
            Caja cajaDia = new Caja();
            cajaDia.setFechaApertura(LocalDateTime.of(dia, LocalTime.of(7, 0)));
            cajaDia.setSaldoInicial(20 + random.nextInt(181)); // 20..200
            cajaDia.setTotalIngresos(0);
            cajaDia.setTotalEgresos(0);
            cajaDia.setSaldoFinal(0);
            cajaDia.setAbierta(true);
            cajaRepository.save(cajaDia);

            // --- Confirmados ---
            for (int i = 0; i < confirmadosHoy; i++) {
                Pedido p = crearPedidoAleatorio(productos, mesas, TipoPedido.PARA_LLEVAR);
                Pedido guardado = pedidoService.crearPedido(p);

                // Fecha realista del día
                guardado.setFecha(randomHoraDelDia(dia));
                pedidoRepository.save(guardado);

                totalGenerados++;
            }

            // --- Cancelados ---
            for (int i = 0; i < canceladosHoy; i++) {
                TipoPedido tipo = random.nextDouble() < 0.7 ? TipoPedido.MESA : TipoPedido.PARA_LLEVAR;

                Pedido p = crearPedidoAleatorio(productos, mesas, tipo);
                Pedido guardado = pedidoService.crearPedido(p);

                guardado.setFecha(randomHoraDelDia(dia));
                pedidoRepository.save(guardado);

                // Cancelar (libera mesa si aplica)
                pedidoService.cancelarPedido(guardado.getIdPedido());

                totalGenerados++;
            }

            // --- Pagados ---
            for (int i = 0; i < pagadosHoy; i++) {
                TipoPedido tipo = random.nextDouble() < 0.7 ? TipoPedido.MESA : TipoPedido.PARA_LLEVAR;

                Pedido p = crearPedidoAleatorio(productos, mesas, tipo);
                Pedido guardado = pedidoService.crearPedido(p);

                guardado.setFecha(randomHoraDelDia(dia));
                pedidoRepository.save(guardado);

                String metodoPago = random.nextBoolean() ? "EFECTIVO" : "TRANSFERENCIA";

                // Pagar (libera mesa si aplica)
                pedidoService.pagarPedido(guardado.getIdPedido(), metodoPago);

                // Registrar ingreso en caja del día (mismo flujo que tu Controller)
                cajaService.registrarIngreso(guardado.getTotal());

                totalGenerados++;
            }

            // Cerrar caja del día con fecha de ese día (no usando cerrarCaja() porque pone now())
            // Recalcular saldo final: inicial + ingresos - egresos
            Caja cajaActualizada = cajaRepository.findById(cajaDia.getIdCaja()).orElseThrow();
            double saldoFinal = cajaActualizada.getSaldoInicial()
                    + cajaActualizada.getTotalIngresos()
                    - cajaActualizada.getTotalEgresos();

            cajaActualizada.setSaldoFinal(saldoFinal);
            cajaActualizada.setFechaCierre(LocalDateTime.of(dia, LocalTime.of(23, 59, 59)));
            cajaActualizada.setAbierta(false);
            cajaRepository.save(cajaActualizada);
        }

        // Si por algún motivo quedó carryCancel (muy improbable), lo avisa
        if (carryCancel > 0) {
            System.out.println("[Seeder] Aviso: quedaron " + carryCancel + " cancelados sin asignar (ajusta DAYS o distribución).");
        }

        System.out.println("[Seeder] Listo. Pedidos generados: " + totalGenerados);
    }

    private void asegurarMesas(int total) {
        for (int i = 1; i <= total; i++) {
            Mesa existente = mesaRepository.findByNumero(i);
            if (existente == null) {
                Mesa m = new Mesa();
                m.setNumero(i);
                m.setEstado(EstadoMesa.LIBRE);
                mesaRepository.save(m);
            }
        }
    }

    private void asegurarCategorias(int total) {
        // Como nombre es UNIQUE, usamos prefijo SEED_ para no chocar con datos previos
        Set<String> existentes = new HashSet<>();
        for (Categoria c : categoriaRepository.findAll()) {
            existentes.add(c.getNombre());
        }

        int creadas = 0;
        int idx = 1;
        while (creadas < total) {
            String nombre = String.format("SEED_CAT_%02d", idx++);
            if (!existentes.contains(nombre)) {
                Categoria c = new Categoria();
                c.setNombre(nombre);
                c.setDescripcion("Categoría generada por seeder");
                c.setActiva(true);
                categoriaRepository.save(c);
                existentes.add(nombre);
                creadas++;
            }
        }
    }

    private void asegurarProductos(int total) {
        List<Categoria> cats = categoriaRepository.findAll();
        if (cats.isEmpty()) throw new IllegalStateException("No hay categorías para crear productos.");

        // Si ya hay productos, solo completa hasta 50
        long actuales = productoRepository.count();
        int faltan = (int) Math.max(0, total - actuales);

        for (int i = 1; i <= faltan; i++) {
            Producto p = new Producto();
            p.setNombre("SEED_PROD_" + UUID.randomUUID().toString().substring(0, 8));
            p.setPrecio(1.5 + random.nextInt(295) / 10.0); // 1.5..31.0
            p.setDisponible(true);
            p.setImagenBase64(null); // no importa para tus pruebas actuales
            p.setCategoria(cats.get(random.nextInt(cats.size())));
            productoRepository.save(p);
        }
    }

    private Pedido crearPedidoAleatorio(List<Producto> productos, List<Mesa> mesas, TipoPedido tipo) {
        Pedido pedido = new Pedido();
        pedido.setCliente("Cliente_" + UUID.randomUUID().toString().substring(0, 6));
        pedido.setTipoPedido(tipo);

        if (tipo == TipoPedido.MESA) {
            Mesa mesa = mesas.get(random.nextInt(mesas.size()));
            pedido.setMesa(mesa);
        } else {
            pedido.setMesa(null);
        }

        int cantDetalles = 1 + random.nextInt(5); // 1..5
        List<DetallePedido> detalles = new ArrayList<>();

        // Para no repetir producto dentro del mismo pedido
        Set<Long> usados = new HashSet<>();

        while (detalles.size() < cantDetalles) {
            Producto prod = productos.get(random.nextInt(productos.size()));
            if (usados.contains(prod.getIdProducto())) continue;
            usados.add(prod.getIdProducto());

            DetallePedido d = new DetallePedido();
            d.setProducto(prod);
            d.setCantidad(1 + random.nextInt(3)); // 1..3
            detalles.add(d);
        }

        pedido.setDetalles(detalles);
        return pedido;
    }

    private LocalDateTime randomHoraDelDia(LocalDate dia) {
        // 08:00 a 22:00
        int minutos = random.nextInt(14 * 60); // 0..839
        return LocalDateTime.of(dia, LocalTime.of(8, 0)).plusMinutes(minutos);
    }
}

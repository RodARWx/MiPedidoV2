package uce.edu.MiPedido.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.DetallePedido;
import uce.edu.MiPedido.Model.EstadoMesa;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Mesa;
import uce.edu.MiPedido.Model.Pedido;
import uce.edu.MiPedido.Model.Producto;
import uce.edu.MiPedido.Model.TipoPedido;
import uce.edu.MiPedido.Repository.DetallePedidoRepository;
import uce.edu.MiPedido.Repository.MesaRepository;
import uce.edu.MiPedido.Repository.PedidoRepository;
import uce.edu.MiPedido.Repository.ProductoRepository;

@Service
public class PedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    // Calcula el subtotal de un detalle
    public double calcularSubtotal(Producto producto, int cantidad) {
        return producto.getPrecio() * cantidad;
    }

    // Asigna el subtotal al detalle
    public void calcularSubtotalDetalle(DetallePedido detalle) {
        double subtotal = detalle.getProducto().getPrecio()
                * detalle.getCantidad();
        detalle.setSubtotal(subtotal);
    }

    // Calcula el total del pedido
    public double calcularTotalPedido(Pedido pedido) {
        return pedido.getDetalles()
                .stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
    }

    public void agregarProductoAPedido(Long idPedido, Long idProducto, int cantidad) {

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (pedido == null || producto == null || cantidad <= 0) {
            return;
        }

        //VALIDAR ANTES DE MODIFICAR
        validarPedidoEditable(pedido);

        DetallePedido detalle = detallePedidoRepository
                .findByPedidoIdPedidoAndProductoIdProducto(idPedido, idProducto)
                .orElse(null);

        if (detalle != null) {
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalle.setSubtotal(detalle.getCantidad() * producto.getPrecio());
        } else {
            detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setSubtotal(producto.getPrecio() * cantidad);
        }

        detallePedidoRepository.save(detalle);

        // recalcular total
        pedido.setTotal(calcularTotalPedido(pedido));
        pedidoRepository.save(pedido);
    }

    public void actualizarCantidad(Long idDetalle, int cantidad) {

        DetallePedido detalle = detallePedidoRepository.findById(idDetalle).orElse(null);
        if (detalle == null || cantidad <= 0) {
            return;
        }

        Pedido pedido = detalle.getPedido();
        validarPedidoEditable(pedido);

        detalle.setCantidad(cantidad);
        detalle.setSubtotal(cantidad * detalle.getProducto().getPrecio());

        detallePedidoRepository.save(detalle);

        pedido.setTotal(calcularTotalPedido(pedido));
        pedidoRepository.save(pedido);
    }

    public void eliminarDetalle(Long idDetalle, Long idPedido) {

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);

        if (pedido == null) {
            return;
        }

        //VALIDAR ANTES DE BORRAR
        validarPedidoEditable(pedido);

        detallePedidoRepository.deleteById(idDetalle);

        pedido.setTotal(calcularTotalPedido(pedido));
        pedidoRepository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    private void validarPedidoEditable(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.PAGADO
                || pedido.getEstado() == EstadoPedido.CANCELADO) {

            throw new IllegalStateException(
                    "El pedido no puede modificarse"
            );
        }
    }

    public void confirmarPedido(Long idPedido) {
        Pedido pedido = buscarPorId(idPedido);

        if (pedido == null) {
            return;
        }

        if (pedido.getEstado() != EstadoPedido.ABIERTO) {
            throw new IllegalStateException("Solo se puede confirmar un pedido abierto");
        }

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalStateException("No se puede confirmar un pedido sin productos");
        }

        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedidoRepository.save(pedido);
    }

    public void pagarPedido(Long idPedido) {

        Pedido pedido = buscarPorId(idPedido);

        if (pedido == null) {
            return;
        }

        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new IllegalStateException("Solo se puede pagar un pedido confirmado");
        }

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalStateException("No se puede pagar un pedido sin productos");
        }

        pedido.setEstado(EstadoPedido.PAGADO);

        // Liberar mesa si existe
        if (pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }

        pedidoRepository.save(pedido);
    }

    public Pedido guardarPedido(Pedido pedido) {

        //Protección: nunca permitir tipo nulo
        if (pedido.getTipoPedido() == null) {
            pedido.setTipoPedido(TipoPedido.PARA_LLEVAR);
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido crearPedido(Pedido pedido) {

        validarPedido(pedido);

        pedido.setEstado(EstadoPedido.ABIERTO);
        pedido.setTotal(0);

        // SOLO bloquear mesa si es pedido en mesa
        if (pedido.getTipoPedido() == TipoPedido.MESA) {

            Mesa mesa = mesaRepository
                    .findById(pedido.getMesa().getIdMesa())
                    .orElseThrow(() -> new IllegalStateException("Mesa no encontrada"));

            if (mesa.getEstado() == EstadoMesa.OCUPADA) {
                throw new IllegalStateException("La mesa ya está ocupada");
            }

            mesa.setEstado(EstadoMesa.OCUPADA);
            mesaRepository.save(mesa);

            pedido.setMesa(mesa);
        }

        return pedidoRepository.save(pedido);
    }

    private void validarPedido(Pedido pedido) {

        if (pedido.getTipoPedido() == TipoPedido.MESA && pedido.getMesa() == null) {
            throw new IllegalStateException("Un pedido en mesa debe tener una mesa");
        }

        if (pedido.getTipoPedido() == TipoPedido.PARA_LLEVAR) {
            pedido.setMesa(null);
        }
    }

    public void eliminarPedido(Long idPedido) {

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido == null) {
            return;
        }

        // Si el pedido tenía mesa se liberarla
        if (pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }

        pedidoRepository.delete(pedido);
    }

    public void cancelarPedido(Long idPedido) {

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido == null) {
            return;
        }

        // No se puede cancelar si ya fue pagado
        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido ya pagado");
        }

        // Liberar mesa si tenía
        if (pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

}

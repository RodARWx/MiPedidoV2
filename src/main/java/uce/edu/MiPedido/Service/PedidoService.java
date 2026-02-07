package uce.edu.MiPedido.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.DetallePedido;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Pedido;
import uce.edu.MiPedido.Model.Producto;
import uce.edu.MiPedido.Repository.DetallePedidoRepository;
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
        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalStateException(
                    "El pedido ya fue pagado y no puede modificarse"
            );
        }
    }

    public void confirmarPedido(Long idPedido) {
        Pedido pedido = buscarPorId(idPedido);

        if (pedido.getEstado() == EstadoPedido.ABIERTO) {
            pedido.setEstado(EstadoPedido.CONFIRMADO);
            pedidoRepository.save(pedido);
        }
    }

    public void pagarPedido(Long idPedido) {
        Pedido pedido = buscarPorId(idPedido);

        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoRepository.save(pedido);
    }

}

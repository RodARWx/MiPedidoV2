package uce.edu.MiPedido.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE
import uce.edu.MiPedido.Model.Categoria;
import uce.edu.MiPedido.Model.Producto;
import uce.edu.MiPedido.Repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public boolean esValido(Producto producto) {
        return producto.getNombre() != null
                && !producto.getNombre().isEmpty()
                && producto.getPrecio() > 0
                && producto.getCategoria() != null
                && producto.getCategoria().isActiva();
    }

    @Transactional // Transacción para escritura
    public Producto guardar(Producto producto) {
        if (!esValido(producto)) {
            throw new IllegalArgumentException("Producto no válido");
        }
        return productoRepository.save(producto);
    }

    // --- CORRECCIÓN: Transacciones de lectura ---
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Categoria categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarDisponiblesPorCategoria(Categoria categoria) {
        return productoRepository.findByCategoriaAndDisponibleTrue(categoria);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }
    // ---------------------------------------------

    @Transactional
    public void cambiarDisponibilidad(Long idProducto) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto != null) {
            producto.setDisponible(!producto.isDisponible());
            productoRepository.save(producto);
        }
    }
}

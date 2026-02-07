package uce.edu.MiPedido.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Producto;
import uce.edu.MiPedido.Repository.ProductoRepository;


@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Validación básica del producto
    public boolean esValido(Producto producto) {
        return producto.getNombre() != null
                && !producto.getNombre().isEmpty()
                && producto.getPrecio() > 0
                && producto.getTipo() != null
                && !producto.getTipo().isEmpty();
    }

    // Guardar producto
    public Producto guardar(Producto producto) {
        if (!esValido(producto)) {
            throw new IllegalArgumentException("Producto no válido");
        }
        return productoRepository.save(producto);
    }

    // Listar todos los productos
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // Listar productos por tipo (PLATO / BEBIDA)
    public List<Producto> listarPorTipo(String tipo) {
        return productoRepository.findByTipo(tipo);
    }

    // Listar solo productos disponibles
    public List<Producto> listarDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }

    // Buscar producto por id
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    // Eliminar producto
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
    
}
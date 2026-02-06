package uce.edu.MiPedido.Service;

import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Producto;

@Service
public class ProductoService {

    public boolean esValido(Producto producto) {
        return producto.getNombre() != null
                && !producto.getNombre().isEmpty()
                && producto.getPrecio() > 0;
    }
}

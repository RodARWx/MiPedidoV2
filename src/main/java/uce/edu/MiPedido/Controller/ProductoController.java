package uce.edu.MiPedido.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uce.edu.MiPedido.Model.Producto;
import uce.edu.MiPedido.Service.ProductoService;

@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    //Menú principal de productos
    @GetMapping("/productos")
    public String menuProductos() {
        return "productos";
    }

    //Listar productos
    @GetMapping("/listar_productos")
    public String listarProductos(Model model) {
        model.addAttribute("listaProductos", productoService.listarTodos());
        return "listar_productos";
    }

    //Formulario nuevo producto
    @GetMapping("/nuevo_producto")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "nuevo_producto";
    }

    //Guardar producto
    @PostMapping("/guardar_producto")
    public String guardarProducto(@ModelAttribute Producto producto) {
        productoService.guardar(producto);
        return "redirect:/listar_productos";
    }

    //Eliminar producto
    @GetMapping("/eliminar_producto/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/listar_productos";
    }
    
}

package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Necesario para subir archivos
import uce.edu.MiPedido.Model.Categoria;
import uce.edu.MiPedido.Model.Producto;
import uce.edu.MiPedido.Service.CategoriaService;
import uce.edu.MiPedido.Service.ProductoService;

import java.io.IOException;
import java.util.Base64;

@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/productos")
    public String menuProductos() {
        return "productos";
    }

    @GetMapping("/listar_productos")
    public String listarProductos(Model model) {
        model.addAttribute("listaProductos", productoService.listarTodos());
        return "listar_productos";
    }

    @GetMapping("/nuevo_producto")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "nuevo_producto";
    }

    // --- GUARDAR CON IMAGEN ---
    @PostMapping("/guardar_producto")
    public String guardarProducto(@ModelAttribute Producto producto,
            @RequestParam("categoria") Long idCategoria,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        // 1. Vincular categoría
        Categoria categoria = categoriaService.buscarPorId(idCategoria);
        producto.setCategoria(categoria);

        // 2. Procesar imagen (Si se subió una nueva)
        if (file != null && !file.isEmpty()) {
            try {
                // Convertir imagen a texto Base64
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                producto.setImagenBase64(base64);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Si no hay imagen nueva y estamos editando, mantener la antigua
            if (producto.getIdProducto() != null) {
                Producto pAntiguo = productoService.buscarPorId(producto.getIdProducto());
                if (pAntiguo != null) {
                    producto.setImagenBase64(pAntiguo.getImagenBase64());
                }
            }
        }

        productoService.guardar(producto);
        return "redirect:/listar_productos";
    }

    @GetMapping("/producto/cambiar_disponibilidad/{id}")
    public String cambiarDisponibilidad(@PathVariable Long id) {
        productoService.cambiarDisponibilidad(id);
        return "redirect:/listar_productos";
    }

    @GetMapping("/editar_producto/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "editar_producto";
    }
}

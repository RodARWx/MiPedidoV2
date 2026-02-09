package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import uce.edu.MiPedido.Model.Categoria;
import uce.edu.MiPedido.Service.CategoriaService;

@Controller
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/categorias")
    public String menuCategorias(Model model) {
        model.addAttribute("listaCategorias", categoriaService.listarTodas());
        model.addAttribute("categoria", new Categoria());
        return "categorias";
    }

    @GetMapping("/nueva_categoria")
    public String nuevaCategoria(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "nueva_categoria";
    }

    @PostMapping("/guardar_categoria")
    public String guardarCategoria(@ModelAttribute Categoria categoria) {
        categoriaService.guardar(categoria);
        return "redirect:/categorias";
    }

    @GetMapping("/editar_categoria/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {

        Categoria categoria = categoriaService.buscarPorId(id);

        if (categoria == null) {
            return "redirect:/categorias";
        }

        model.addAttribute("categoria", categoria);
        return "editar_categoria";
    }

    @GetMapping("/categoria/cambiar_estado/{id}")
    public String cambiarEstado(@PathVariable Long id) {
        categoriaService.cambiarEstado(id);
        return "redirect:/categorias";
    }
}

package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uce.edu.MiPedido.Model.Caja;
import uce.edu.MiPedido.Model.EstadoMesa;
import uce.edu.MiPedido.Model.Usuario;
import uce.edu.MiPedido.Repository.UsuarioRepository;
import uce.edu.MiPedido.Service.CajaService;
import uce.edu.MiPedido.Service.MesaService;
import uce.edu.MiPedido.Service.ProductoService;

@Controller
public class HomeController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private CajaService cajaService;

    @GetMapping("/")
    public String index(Model model) {
        // Datos para el Dashboard
        model.addAttribute("totalProductos", productoService.listarTodos().size());
        model.addAttribute("mesasOcupadas", mesaService.listarTodas().stream().filter(m -> m.getEstado() == EstadoMesa.OCUPADA).count());

        // Validar estado de caja para mostrar alerta
        Caja caja = cajaService.obtenerCajaAbierta();
        model.addAttribute("cajaAbierta", caja != null);

        return "index";
    }
}


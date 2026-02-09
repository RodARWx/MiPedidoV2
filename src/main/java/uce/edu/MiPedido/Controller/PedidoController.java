package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Pedido;
import uce.edu.MiPedido.Model.Rol;
import uce.edu.MiPedido.Model.Usuario;
import uce.edu.MiPedido.Repository.PedidoRepository;
import uce.edu.MiPedido.Service.MesaService;
import uce.edu.MiPedido.Service.PedidoService;
import uce.edu.MiPedido.Service.ProductoService;
import uce.edu.MiPedido.Service.UsuarioService;
import uce.edu.MiPedido.Service.UsuarioSesionService;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioSesionService usuarioSesionService;

    //Menú de pedidos
    @GetMapping("/pedidos")
    public String menuPedidos(Model model) {
        model.addAttribute("usuario", usuarioSesionService.getUsuarioActual());
        return "pedidos";
    }

    @GetMapping("/listar_pedidos")
    public String listarPedidos(
            @RequestParam(required = false) java.util.List<EstadoPedido> estados,
            Model model) {

        if (estados == null || estados.isEmpty()) {
            model.addAttribute("listaPedidos", pedidoService.buscarTodos());
        } else {
            model.addAttribute("listaPedidos", pedidoService.listarPorEstados(estados));
        }

        model.addAttribute("estadosSeleccionados", estados);

        return "listar_pedidos";
    }

    //Nuevo pedido
    @GetMapping("/nuevo_pedido")
    public String nuevoPedido(Model model) {
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("mesasLibres", mesaService.listarLibres());
        return "nuevo_pedido";
    }

    //Guardar pedido
    @PostMapping("/guardar_pedido")
    public String guardarPedido(
            @ModelAttribute Pedido pedido,
            RedirectAttributes redirect
    ) {
        try {
            Pedido nuevo = pedidoService.crearPedido(pedido);
            return "redirect:/pedidos/" + nuevo.getIdPedido();
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/nuevo_pedido";
        }
    }

    //Eliminar pedido
    @GetMapping("/eliminar_pedido/{id}")
    public String eliminarPedido(@PathVariable Long id) {
        pedidoRepository.deleteById(id);
        return "redirect:/listar_pedidos";
    }

    //Ver pedido + detalles
    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model) {

        Pedido pedido = pedidoRepository.findById(id).orElse(null);

        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", pedido.getDetalles());
        model.addAttribute("productos", productoService.listarDisponibles());

        return "detalle_pedido";
    }

    //Agregar producto al pedido
    @PostMapping("/agregar_producto")
    public String agregarProducto(
            @RequestParam Long idPedido,
            @RequestParam Long idProducto,
            @RequestParam int cantidad,
            RedirectAttributes redirect
    ) {
        try {
            pedidoService.agregarProductoAPedido(idPedido, idProducto, cantidad);
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + idPedido;
    }

    @PostMapping("/eliminar_detalle/{idDetalle}")
    public String eliminarDetalle(
            @PathVariable Long idDetalle,
            @RequestParam Long idPedido,
            RedirectAttributes redirect
    ) {
        try {
            pedidoService.eliminarDetalle(idDetalle, idPedido);
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + idPedido;
    }

    @PostMapping("/pedidos/actualizar_cantidad")
    public String actualizarCantidad(@RequestParam Long idDetalle,
            @RequestParam int cantidad,
            @RequestParam Long idPedido) {

        pedidoService.actualizarCantidad(idDetalle, cantidad);
        return "redirect:/pedidos/" + idPedido;
    }

    @GetMapping("/pedido/confirmar/{id}")
    public String confirmarPedido(@PathVariable Long id) {
        pedidoService.confirmarPedido(id);
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/pedidos/pagar/{id}")
    public String pagarPedido(
            @PathVariable Long id,
            RedirectAttributes redirect
    ) {
        try {
            pedidoService.pagarPedido(id);
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @GetMapping("/pedido/cancelar/{id}")
    public String cancelarPedido(
            @PathVariable Long id,
            RedirectAttributes redirect
    ) {
        try {
            pedidoService.cancelarPedido(id);
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @ModelAttribute("usuarioActivo")
    public Usuario usuarioActivoSimulado() {

        Usuario usuario = new Usuario();
        usuario.setUsername("admin");     // o "mesero1"
        usuario.setRol(Rol.MESERO);        // cambia a Rol.MESERO para probar
        usuario.setActivo(true);

        return usuario;
    }

}

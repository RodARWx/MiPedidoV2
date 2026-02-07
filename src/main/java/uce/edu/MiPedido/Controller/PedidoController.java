package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uce.edu.MiPedido.Model.Pedido;
import uce.edu.MiPedido.Repository.PedidoRepository;
import uce.edu.MiPedido.Service.PedidoService;
import uce.edu.MiPedido.Service.ProductoService;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    //Menú de pedidos
    @GetMapping("/pedidos")
    public String menuPedidos() {
        return "pedidos";
    }

    //Listar pedidos
    @GetMapping("/listar_pedidos")
    public String listarPedidos(Model model) {
        model.addAttribute("listaPedidos", pedidoRepository.findAll());
        return "listar_pedidos";
    }

    //Nuevo pedido
    @GetMapping("/nuevo_pedido")
    public String nuevoPedido(Model model) {
        model.addAttribute("pedido", new Pedido());
        return "nuevo_pedido";
    }

    //Guardar pedido
    @PostMapping("/guardar_pedido")
    public String guardarPedido(@ModelAttribute Pedido pedido) {
        pedidoRepository.save(pedido);
        return "redirect:/listar_pedidos";
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
    @PostMapping("/pedidos/agregar_producto")
    public String agregarProducto(@RequestParam Long idPedido,
            @RequestParam Long idProducto,
            @RequestParam int cantidad) {

        pedidoService.agregarProductoAPedido(idPedido, idProducto, cantidad);
        return "redirect:/pedidos/" + idPedido;
    }

}

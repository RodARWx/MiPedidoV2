package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uce.edu.MiPedido.Service.MesaService;

@Controller
public class MesaController {

    @Autowired
    private MesaService mesaService;

    // Pantalla de gestión de mesas
    @GetMapping("/mesas")
    public String gestionarMesas(Model model) {
        model.addAttribute("mesas", mesaService.listarTodas());
        return "gestionar_mesas";
    }

    // Crear nueva mesa
    @PostMapping("/mesas/crear")
    public String crearMesa(
            @RequestParam int numero,
            RedirectAttributes redirect
    ) {
        try {
            mesaService.crearMesa(numero);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }
}

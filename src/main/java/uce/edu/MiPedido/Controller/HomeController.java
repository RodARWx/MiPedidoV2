package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uce.edu.MiPedido.Model.Usuario;
import uce.edu.MiPedido.Repository.UsuarioRepository;

@Controller
public class HomeController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String index(Model model) {

        // SIMULACIÓN TEMPORAL DEL USUARIO ACTIVO
        Usuario usuarioActivo = usuarioRepository
                .findByUsername("admin")
                .orElse(null);

        model.addAttribute("usuarioActivo", usuarioActivo);

        return "index";
    }
}

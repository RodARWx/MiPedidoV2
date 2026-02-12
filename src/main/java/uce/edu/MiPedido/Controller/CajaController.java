package uce.edu.MiPedido.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uce.edu.MiPedido.Model.Caja;
import uce.edu.MiPedido.Service.CajaService;
import uce.edu.MiPedido.Service.UsuarioSesionService;

@Controller
@RequestMapping("/caja")
public class CajaController {

    @Autowired private CajaService cajaService;
    @Autowired private UsuarioSesionService usuarioSesionService;

    @GetMapping
    public String verCaja(Model model) {
        Caja cajaActual = cajaService.obtenerCajaAbierta();
        model.addAttribute("caja", cajaActual);
        return "caja"; // Vista caja.html
    }

    @PostMapping("/abrir")
    public String abrirCaja(@RequestParam double saldoInicial) {
        cajaService.abrirCaja(saldoInicial, usuarioSesionService.getUsuarioActual());
        return "redirect:/caja";
    }

    @PostMapping("/cerrar")
    public String cerrarCaja(@RequestParam Long idCaja) {
        cajaService.cerrarCaja(idCaja);
        return "redirect:/caja";
    }

    @PostMapping("/gasto")
    public String registrarGasto(@RequestParam double monto, @RequestParam String descripcion) {
        cajaService.registrarGasto(monto, descripcion);
        return "redirect:/caja";
    }
}
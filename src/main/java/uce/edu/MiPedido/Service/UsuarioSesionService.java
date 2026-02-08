package uce.edu.MiPedido.Service;

import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Rol;
import uce.edu.MiPedido.Model.Usuario;

@Service
public class UsuarioSesionService {

    // Usuario simulado en memoria
    private Usuario usuarioActual;

    // Simular login como ADMIN
    public void loginComoAdmin() {
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);

        this.usuarioActual = admin;
    }

    // Simular login como MESERO
    public void loginComoMesero() {
        Usuario mesero = new Usuario();
        mesero.setUsername("mesero");
        mesero.setRol(Rol.MESERO);
        mesero.setActivo(true);

        this.usuarioActual = mesero;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean esAdmin() {
        return usuarioActual != null && usuarioActual.getRol() == Rol.ADMIN;
    }

    public boolean esMesero() {
        return usuarioActual != null && usuarioActual.getRol() == Rol.MESERO;
    }
    
}

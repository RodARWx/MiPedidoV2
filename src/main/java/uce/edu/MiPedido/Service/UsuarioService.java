package uce.edu.MiPedido.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Rol;
import uce.edu.MiPedido.Model.Usuario;
import uce.edu.MiPedido.Repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //Crear ADMIN por defecto si no existe
    public void crearAdminSiNoExiste() {

        boolean existe = usuarioRepository
                .findByUsername("admin")
                .isPresent();

        if (!existe) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("admin"); // luego se cambia
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);

            usuarioRepository.save(admin);
        }
    }

    //Crear mesero
    public Usuario crearMesero(String username, String password) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("El usuario ya existe");
        }

        Usuario mesero = new Usuario();
        mesero.setUsername(username);
        mesero.setPassword(password);
        mesero.setRol(Rol.MESERO);
        mesero.setActivo(true);

        return usuarioRepository.save(mesero);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public void desactivarUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario != null) {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        }
    }
}

package uce.edu.MiPedido.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}

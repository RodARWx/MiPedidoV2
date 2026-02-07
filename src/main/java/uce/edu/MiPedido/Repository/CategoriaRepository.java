package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Categoria;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByActivaTrue();
}


package uce.edu.MiPedido.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Categoria;
import uce.edu.MiPedido.Model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByDisponibleTrue();

    List<Producto> findByCategoria(Categoria categoria);

    List<Producto> findByCategoriaAndDisponibleTrue(Categoria categoria);
}

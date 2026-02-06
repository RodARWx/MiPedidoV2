
package uce.edu.MiPedido.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Producto;


public interface ProductoRepository extends JpaRepository<Producto, Long>{
    // Buscar productos por tipo (PLATO o BEBIDA)
    List<Producto> findByTipo(String tipo);

    // Buscar solo productos disponibles
    List<Producto> findByDisponibleTrue();
}

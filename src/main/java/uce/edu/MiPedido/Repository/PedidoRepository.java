
package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long>{
    
}

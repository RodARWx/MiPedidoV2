package uce.edu.MiPedido.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long>{
    
    // Buscar detalles por pedido
    List<DetallePedido> findByPedidoIdPedido(Long idPedido);
    
    Optional<DetallePedido> findByPedidoIdPedidoAndProductoIdProducto(
        Long idPedido, Long idProducto);

}

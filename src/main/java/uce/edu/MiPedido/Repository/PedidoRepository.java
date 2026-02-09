package uce.edu.MiPedido.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoIn(List<EstadoPedido> estados);

}

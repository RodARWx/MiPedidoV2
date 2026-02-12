package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Pedido;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoIn(List<EstadoPedido> estados);

    // REPORTE 1: Platos más vendidos 
    @Query("SELECT d.producto.nombre, SUM(d.cantidad) as totalVendido "
            + "FROM DetallePedido d "
            + "GROUP BY d.producto.nombre "
            + "ORDER BY totalVendido DESC")
    List<Object[]> obtenerPlatosMasVendidos();

    // REPORTE 2: Ganancias por fecha (Días más rentables) 
    @Query("SELECT FUNCTION('DATE', p.fecha) as dia, SUM(p.total) as ganancia "
            + "FROM Pedido p WHERE p.estado = 'PAGADO' "
            + "GROUP BY FUNCTION('DATE', p.fecha) "
            + "ORDER BY ganancia DESC")
    List<Object[]> obtenerDiasMasRentables();
}

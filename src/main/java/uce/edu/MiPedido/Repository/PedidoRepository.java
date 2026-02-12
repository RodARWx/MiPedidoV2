package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uce.edu.MiPedido.Model.EstadoPedido;
import uce.edu.MiPedido.Model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoIn(List<EstadoPedido> estados);

    // --- NUEVO: Buscar pedidos en un rango de fechas ---
    List<Pedido> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // REPORTE 1: Platos más vendidos (General - Histórico)
    @Query("SELECT d.producto.nombre, SUM(d.cantidad) as totalVendido "
            + "FROM DetallePedido d "
            + "GROUP BY d.producto.nombre "
            + "ORDER BY totalVendido DESC")
    List<Object[]> obtenerPlatosMasVendidos();

    // REPORTE 1.1: Platos más vendidos (POR FECHA)
    @Query("SELECT d.producto.nombre, SUM(d.cantidad) as totalVendido "
            + "FROM DetallePedido d "
            + "JOIN d.pedido p "
            + "WHERE p.fecha BETWEEN :inicio AND :fin "
            + "GROUP BY d.producto.nombre "
            + "ORDER BY totalVendido DESC")
    List<Object[]> obtenerPlatosMasVendidosPorFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // REPORTE 2: Ganancias por fecha
    @Query("SELECT FUNCTION('DATE', p.fecha) as dia, SUM(p.total) as ganancia "
            + "FROM Pedido p WHERE p.estado = 'PAGADO' "
            + "GROUP BY FUNCTION('DATE', p.fecha) "
            + "ORDER BY ganancia DESC")
    List<Object[]> obtenerDiasMasRentables();
}

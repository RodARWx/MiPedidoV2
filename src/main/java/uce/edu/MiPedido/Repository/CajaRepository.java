package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Caja;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findByAbiertaTrue();

    // --- NUEVO: Buscar cajas por rango de fechas ---
    List<Caja> findByFechaAperturaBetween(LocalDateTime inicio, LocalDateTime fin);
}

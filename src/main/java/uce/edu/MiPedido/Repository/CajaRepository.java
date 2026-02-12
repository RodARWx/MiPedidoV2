package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Caja;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    // Buscar si hay una caja abierta actualmente
    Optional<Caja> findByAbiertaTrue();
}

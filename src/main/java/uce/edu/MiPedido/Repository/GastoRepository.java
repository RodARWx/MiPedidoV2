package uce.edu.MiPedido.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.Gasto;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
}

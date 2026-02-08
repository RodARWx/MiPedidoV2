package uce.edu.MiPedido.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.MiPedido.Model.EstadoMesa;
import uce.edu.MiPedido.Model.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByEstado(EstadoMesa estado);

    Mesa findByNumero(int numero);
}

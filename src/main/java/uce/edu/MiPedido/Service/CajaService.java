package uce.edu.MiPedido.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Caja;
import uce.edu.MiPedido.Model.Gasto;
import uce.edu.MiPedido.Model.Usuario;
import uce.edu.MiPedido.Repository.CajaRepository;
import uce.edu.MiPedido.Repository.GastoRepository;

import java.time.LocalDateTime;

@Service
public class CajaService {

    @Autowired
    private CajaRepository cajaRepository;
    @Autowired
    private GastoRepository gastoRepository;

    public Caja obtenerCajaAbierta() {
        return cajaRepository.findByAbiertaTrue().orElse(null);
    }

    public Caja abrirCaja(double saldoInicial, Usuario usuario) {
        if (obtenerCajaAbierta() != null) {
            throw new IllegalStateException("Ya existe una caja abierta");
        }
        Caja caja = new Caja();
        caja.setSaldoInicial(saldoInicial);
        caja.setUsuarioApertura(usuario);
        caja.setAbierta(true);
        return cajaRepository.save(caja);
    }

    public void cerrarCaja(Long idCaja) {
        Caja caja = cajaRepository.findById(idCaja).orElseThrow();

        // Calcular saldo final: Inicial + Ingresos - Egresos
        double saldoFinal = caja.getSaldoInicial() + caja.getTotalIngresos() - caja.getTotalEgresos();

        caja.setSaldoFinal(saldoFinal);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setAbierta(false);
        cajaRepository.save(caja);
    }

    public void registrarGasto(double monto, String descripcion) {
        Caja caja = obtenerCajaAbierta();
        if (caja == null) {
            throw new IllegalStateException("No hay caja abierta para registrar gastos");
        }

        Gasto gasto = new Gasto();
        gasto.setMonto(monto);
        gasto.setDescripcion(descripcion);
        gasto.setCaja(caja);
        gastoRepository.save(gasto);

        caja.setTotalEgresos(caja.getTotalEgresos() + monto);
        cajaRepository.save(caja);
    }

    // Método llamado desde PedidoService cuando se paga un pedido
    public void registrarIngreso(double monto) {
        Caja caja = obtenerCajaAbierta();
        if (caja != null) {
            caja.setTotalIngresos(caja.getTotalIngresos() + monto);
            cajaRepository.save(caja);
        }
    }
}

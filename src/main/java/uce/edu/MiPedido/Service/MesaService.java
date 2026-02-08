package uce.edu.MiPedido.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.EstadoMesa;
import uce.edu.MiPedido.Model.Mesa;
import uce.edu.MiPedido.Repository.MesaRepository;

@Service
public class MesaService {

    @Autowired
    private MesaRepository mesaRepository;

    public List<Mesa> listarTodas() {
        return mesaRepository.findAll();
    }

    public List<Mesa> listarLibres() {
        return mesaRepository.findByEstado(EstadoMesa.LIBRE);
    }

    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id).orElse(null);
    }

    public Mesa crearMesa(int numero) {

        if (numero <= 0) {
            throw new IllegalArgumentException("Número de mesa inválido");
        }

        if (mesaRepository.count() >= 15) {
            throw new IllegalStateException("Máximo de mesas alcanzado");
        }

        Mesa mesa = new Mesa();
        mesa.setNumero(numero);
        mesa.setEstado(EstadoMesa.LIBRE);

        return mesaRepository.save(mesa);
    }

    public void ocuparMesa(Mesa mesa) {
        mesa.setEstado(EstadoMesa.OCUPADA);
        mesaRepository.save(mesa);
    }

    public void liberarMesa(Mesa mesa) {
        mesa.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesa);
    }
}

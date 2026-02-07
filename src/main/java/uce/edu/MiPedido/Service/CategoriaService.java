package uce.edu.MiPedido.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.MiPedido.Model.Categoria;
import uce.edu.MiPedido.Repository.CategoriaRepository;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivaTrue();
    }

    public void guardar(Categoria categoria) {
        categoriaRepository.save(categoria);
    }

    public void cambiarEstado(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria != null) {
            categoria.setActiva(!categoria.isActiva());
            categoriaRepository.save(categoria);
        }
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }
}

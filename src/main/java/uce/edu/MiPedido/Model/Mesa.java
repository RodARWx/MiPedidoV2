package uce.edu.MiPedido.Model;

import jakarta.persistence.*;

@Entity
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMesa;

    @Column(unique = true, nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    private EstadoMesa estado = EstadoMesa.LIBRE;

    public Long getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Long idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }
}

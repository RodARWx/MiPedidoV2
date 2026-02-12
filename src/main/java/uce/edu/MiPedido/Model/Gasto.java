package uce.edu.MiPedido.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gasto")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGasto;
    private String descripcion;
    private double monto;
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "id_caja")
    private Caja caja;

    public Gasto() {
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdGasto() {
        return idGasto;
    }

    public void setIdGasto(Long idGasto) {
        this.idGasto = idGasto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
    }
}

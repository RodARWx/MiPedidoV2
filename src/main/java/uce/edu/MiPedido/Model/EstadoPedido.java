package uce.edu.MiPedido.Model;

public enum EstadoPedido {
    CONFIRMADO,   // pedido listo, pero aún editable
    PAGADO, 
    CANCELADO// pedido cerrado, no se puede modificar
}

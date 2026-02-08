package uce.edu.MiPedido.Model;

public enum EstadoPedido {
    ABIERTO,      // el mesero está tomando el pedido
    CONFIRMADO,   // pedido listo, pero aún editable
    PAGADO, 
    CANCELADO// pedido cerrado, no se puede modificar
}

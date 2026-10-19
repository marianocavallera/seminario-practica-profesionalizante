package kioscsys.modelo;

import java.math.BigDecimal;

/**
 * Contrato para todo elemento del negocio que se puede valorizar a costo
 * y a precio de venta (RF-04, RF-09).
 *
 * La implementan Producto (valor del stock en góndola) y DetalleVenta
 * (valor de una línea vendida). Gracias a esta interfaz, el cálculo del
 * margen se escribe una sola vez y sirve para ambos (polimorfismo).
 */
public interface Valorizable {

    /** Valor calculado con el costo unitario. */
    BigDecimal calcularValorCosto();

    /** Valor calculado con el precio de venta. */
    BigDecimal calcularValorVenta();

    /** Margen = valor de venta - valor de costo. Método por defecto común a todos. */
    default BigDecimal calcularMargen() {
        return calcularValorVenta().subtract(calcularValorCosto());
    }
}

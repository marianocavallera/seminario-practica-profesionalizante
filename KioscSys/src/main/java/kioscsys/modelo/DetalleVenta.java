package kioscsys.modelo;

import java.math.BigDecimal;

/**
 * Línea de una venta: un producto, su cantidad y el precio y el costo
 * vigentes EN ESE MOMENTO.
 *
 * El precio y el costo se copian al crear el detalle (y son final) porque
 * son datos históricos: si mañana el producto cambia de precio, la ganancia
 * de esta venta no debe cambiar. Es la misma decisión que se tomó en la
 * tabla detalle_venta de la base de datos.
 */
public class DetalleVenta implements Valorizable {

    private final Producto producto;
    private int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal costoUnitario;

    public DetalleVenta(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El detalle debe tener un producto.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        this.costoUnitario = producto.getCostoUnitario();
    }

    /** Se usa cuando el vendedor vuelve a pasar un producto que ya estaba en la venta. */
    void sumarCantidad(int adicional) {
        this.cantidad += adicional;
    }

    public BigDecimal calcularSubtotal() {
        return calcularValorVenta();
    }

    @Override
    public BigDecimal calcularValorCosto() {
        return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    @Override
    public BigDecimal calcularValorVenta() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }
}

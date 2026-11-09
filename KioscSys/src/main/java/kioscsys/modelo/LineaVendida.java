package kioscsys.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una línea de una venta ya registrada, tal como se lee de la base para
 * armar los reportes (CU-10). Guarda el precio y el costo históricos de
 * detalle_venta, no los vigentes del producto.
 *
 * Implementa Valorizable, igual que Producto y DetalleVenta: por eso el
 * margen se calcula con el mismo método por defecto de la interfaz y los
 * totales del día se obtienen con ValorizacionController.calcularTotales,
 * sin escribir otra vez la misma cuenta (reutilización por polimorfismo).
 */
public class LineaVendida implements Valorizable {

    private final int idVenta;
    private final LocalDateTime fecha;
    private final String producto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal costoUnitario;

    public LineaVendida(int idVenta, LocalDateTime fecha, String producto, int cantidad,
                        BigDecimal precioUnitario, BigDecimal costoUnitario) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitario = costoUnitario;
    }

    @Override
    public BigDecimal calcularValorCosto() {
        return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    @Override
    public BigDecimal calcularValorVenta() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Hora (0 a 23) en que se hizo la venta: se usa como índice de los arreglos del reporte. */
    public int getHora() {
        return fecha.getHour();
    }

    public int getIdVenta() {
        return idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }
}

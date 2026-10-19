package kioscsys.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kioscsys.excepciones.StockInsuficienteException;

/**
 * Venta realizada en el mostrador (CU-03).
 *
 * Se compone de sus detalles (composición del diagrama de clases): los
 * detalles se crean y se eliminan solo a través de la venta, y la lista
 * nunca se entrega para ser modificada desde afuera (encapsulamiento).
 */
public class Venta {

    private int idVenta;
    private final LocalDateTime fecha;
    private final Usuario usuario;
    private final List<DetalleVenta> detalles;

    public Venta(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Toda venta debe tener un vendedor responsable.");
        }
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
        this.detalles = new ArrayList<>();
    }

    /**
     * Agrega un producto a la venta. Si el producto ya estaba, suma la cantidad
     * a la misma línea (en la base, la clave de detalle_venta es venta + producto,
     * así que no puede haber dos líneas del mismo producto).
     */
    public void agregarDetalle(Producto producto, int cantidad) throws StockInsuficienteException {
        DetalleVenta existente = buscarDetalle(producto);
        int cantidadTotal = (existente == null) ? cantidad : existente.getCantidad() + cantidad;

        if (!producto.hayStock(cantidadTotal)) {
            throw new StockInsuficienteException(producto.getNombre(),
                    producto.getStockActual(), cantidadTotal);
        }

        if (existente == null) {
            detalles.add(new DetalleVenta(producto, cantidad));
        } else {
            existente.sumarCantidad(cantidad);
        }
    }

    public void quitarDetalle(int indice) {
        if (indice >= 0 && indice < detalles.size()) {
            detalles.remove(indice);
        }
    }

    /** Búsqueda secuencial de la línea de un producto dentro de la venta. */
    private DetalleVenta buscarDetalle(Producto producto) {
        for (DetalleVenta d : detalles) {
            if (d.getProducto().getIdProducto() == producto.getIdProducto()) {
                return d;
            }
        }
        return null;
    }

    /** El total no se guarda: se calcula, igual que en la base de datos. */
    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            total = total.add(d.calcularSubtotal());
        }
        return total;
    }

    /** Ganancia de la venta usando el costo histórico de cada línea (RF-09). */
    public BigDecimal calcularGanancia() {
        BigDecimal ganancia = BigDecimal.ZERO;
        for (Valorizable linea : detalles) {   // polimorfismo vía la interfaz
            ganancia = ganancia.add(linea.calcularMargen());
        }
        return ganancia;
    }

    public int getCantidadUnidades() {
        int unidades = 0;
        for (DetalleVenta d : detalles) {
            unidades += d.getCantidad();
        }
        return unidades;
    }

    public boolean estaVacia() {
        return detalles.isEmpty();
    }

    /** Vista de solo lectura: desde afuera se puede recorrer, pero no modificar. */
    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}

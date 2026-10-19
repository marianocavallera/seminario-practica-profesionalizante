package kioscsys.modelo;

import java.math.BigDecimal;
import kioscsys.excepciones.StockInsuficienteException;

/**
 * Producto del catálogo del kiosco: el concepto central del modelo de dominio.
 *
 * ENCAPSULAMIENTO: los atributos son privados y solo cambian a través de
 * métodos que validan las reglas del negocio. Por ejemplo, el stock no tiene
 * un setter libre: solo se modifica con descontarStock() o sumarStock(), que
 * impiden que quede negativo.
 *
 * Implementa Valorizable para calcular el valor de su stock (RF-04).
 */
public class Producto implements Valorizable {

    private int idProducto;               // 0 mientras no esté guardado en la base
    private String codigoBarras;
    private String nombre;
    private Categoria categoria;
    private BigDecimal costoUnitario;     // BigDecimal = DECIMAL(10,2) en MySQL
    private BigDecimal precioVenta;
    private int stockActual;
    private int stockMinimo;
    private boolean activo;

    /** Constructor para un producto nuevo, todavía no guardado en la base. */
    public Producto(String codigoBarras, String nombre, Categoria categoria,
                    BigDecimal costoUnitario, BigDecimal precioVenta,
                    int stockActual, int stockMinimo) {
        this(0, codigoBarras, nombre, categoria, costoUnitario, precioVenta,
             stockActual, stockMinimo, true);
    }

    /** Constructor completo, usado al leer un producto desde la base de datos. */
    public Producto(int idProducto, String codigoBarras, String nombre, Categoria categoria,
                    BigDecimal costoUnitario, BigDecimal precioVenta,
                    int stockActual, int stockMinimo, boolean activo) {
        if (stockActual < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo.");
        }
        this.idProducto = idProducto;
        setCodigoBarras(codigoBarras);
        setNombre(nombre);
        setCategoria(categoria);
        setCostoUnitario(costoUnitario);
        setPrecioVenta(precioVenta);
        setStockMinimo(stockMinimo);
        this.stockActual = stockActual;
        this.activo = activo;
    }

    // ------------------------------------------------------------------
    // Comportamiento del negocio
    // ------------------------------------------------------------------

    /** ¿Alcanza el stock para vender esta cantidad? */
    public boolean hayStock(int cantidad) {
        return cantidad <= stockActual;
    }

    /** Descuenta unidades vendidas (RN-02). No permite dejar el stock negativo (RN-05). */
    public void descontarStock(int cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero.");
        }
        if (!hayStock(cantidad)) {
            throw new StockInsuficienteException(nombre, stockActual, cantidad);
        }
        stockActual -= cantidad;
    }

    /** Suma unidades (compras, anulación de ventas). */
    public void sumarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a sumar debe ser mayor que cero.");
        }
        stockActual += cantidad;
    }

    /** Alerta de reposición (RF-10). */
    public boolean estaBajoMinimo() {
        return stockActual < stockMinimo;
    }

    public BigDecimal calcularMargenUnitario() {
        return precioVenta.subtract(costoUnitario);
    }

    @Override
    public BigDecimal calcularValorCosto() {
        return costoUnitario.multiply(BigDecimal.valueOf(stockActual));
    }

    @Override
    public BigDecimal calcularValorVenta() {
        return precioVenta.multiply(BigDecimal.valueOf(stockActual));
    }

    // ------------------------------------------------------------------
    // Getters y setters con validación
    // ------------------------------------------------------------------

    public int getIdProducto() {
        return idProducto;
    }

    /** Solo lo usa el DAO, para asignar el id generado por MySQL al guardar. */
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public final void setCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            throw new IllegalArgumentException("El código de barras es obligatorio.");
        }
        this.codigoBarras = codigoBarras.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public final void setCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("El producto debe tener una categoría.");
        }
        this.categoria = categoria;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public final void setCostoUnitario(BigDecimal costoUnitario) {
        if (costoUnitario == null || costoUnitario.signum() < 0) {
            throw new IllegalArgumentException("El costo no puede estar vacío ni ser negativo.");
        }
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    /** RN-01: el producto necesita precio para poder venderse y calcular el margen. */
    public final void setPrecioVenta(BigDecimal precioVenta) {
        if (precioVenta == null || precioVenta.signum() < 0) {
            throw new IllegalArgumentException("El precio de venta no puede estar vacío ni ser negativo.");
        }
        this.precioVenta = precioVenta;
    }

    public int getStockActual() {
        return stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public final void setStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

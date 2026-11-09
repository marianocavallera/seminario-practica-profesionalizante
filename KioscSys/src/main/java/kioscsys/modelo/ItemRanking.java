package kioscsys.modelo;

import java.math.BigDecimal;

/** Un producto dentro del ranking de más vendidos del día (CU-10). */
public class ItemRanking {

    private final String producto;
    private int unidades;
    private BigDecimal importe = BigDecimal.ZERO;
    private BigDecimal ganancia = BigDecimal.ZERO;

    public ItemRanking(String producto) {
        this.producto = producto;
    }

    /** Suma al acumulado del producto una línea vendida. */
    public void acumular(LineaVendida linea) {
        unidades += linea.getCantidad();
        importe = importe.add(linea.calcularValorVenta());
        ganancia = ganancia.add(linea.calcularMargen());
    }

    public String getProducto() {
        return producto;
    }

    public int getUnidades() {
        return unidades;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getGanancia() {
        return ganancia;
    }
}

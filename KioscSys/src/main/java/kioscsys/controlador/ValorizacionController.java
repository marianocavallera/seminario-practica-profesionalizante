package kioscsys.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kioscsys.dao.ProductoDAO;
import kioscsys.modelo.Categoria;
import kioscsys.modelo.Producto;
import kioscsys.modelo.Valorizable;
import kioscsys.util.Algoritmos;

/** Casos de uso CU-09 Consultar valorización de inventario y CU-11 Alertas de stock mínimo. */
public class ValorizacionController {

    private final ProductoDAO productoDAO;

    public ValorizacionController() {
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Productos de una categoría (o todos si es null), ordenados por valor a
     * costo de mayor a menor: arriba queda lo que más capital inmoviliza.
     */
    public List<Producto> listar(Categoria categoria) throws SQLException {
        List<Producto> seleccion = new ArrayList<>();
        for (Producto p : productoDAO.listarActivos()) {
            if (categoria == null || p.getCategoria().equals(categoria)) {
                seleccion.add(p);
            }
        }
        Comparator<Producto> porValor = Comparator.comparing(Producto::calcularValorCosto).reversed();
        return Algoritmos.ordenarPorInsercion(seleccion, porValor);
    }

    /**
     * Suma el valor a costo, a venta y el margen de cualquier lista de
     * elementos Valorizable. Recibe la interfaz, no la clase concreta:
     * el mismo método serviría para valorizar las líneas de una venta.
     *
     * @return arreglo {totalCosto, totalVenta, margenTotal}.
     */
    public BigDecimal[] calcularTotales(List<? extends Valorizable> elementos) {
        BigDecimal costo = BigDecimal.ZERO;
        BigDecimal venta = BigDecimal.ZERO;
        for (Valorizable v : elementos) {
            costo = costo.add(v.calcularValorCosto());
            venta = venta.add(v.calcularValorVenta());
        }
        return new BigDecimal[]{costo, venta, venta.subtract(costo)};
    }

    /** Productos cuyo stock es inferior al mínimo (RF-10). */
    public List<Producto> alertas(List<Producto> productos) {
        List<Producto> bajoMinimo = new ArrayList<>();
        for (Producto p : productos) {
            if (p.estaBajoMinimo()) {
                bajoMinimo.add(p);
            }
        }
        return bajoMinimo;
    }
}

package kioscsys.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import kioscsys.dao.CategoriaDAO;
import kioscsys.dao.ProductoDAO;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.modelo.Categoria;
import kioscsys.modelo.Producto;
import kioscsys.util.Algoritmos;

/** Caso de uso CU-05 Gestionar productos (alta, modificación, baja y búsqueda). */
public class ProductoController {

    /** Criterios de orden que el usuario puede elegir en la pantalla. */
    public enum Orden {
        NOMBRE("Nombre (A-Z)", Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER)),
        STOCK("Stock (menor a mayor)", Comparator.comparingInt(Producto::getStockActual)),
        PRECIO("Precio (mayor a menor)", Comparator.comparing(Producto::getPrecioVenta).reversed()),
        MARGEN("Margen unitario (mayor a menor)", Comparator.comparing(Producto::calcularMargenUnitario).reversed());

        private final String etiqueta;
        private final Comparator<Producto> comparador;

        Orden(String etiqueta, Comparator<Producto> comparador) {
            this.etiqueta = etiqueta;
            this.comparador = comparador;
        }

        public Comparator<Producto> getComparador() {
            return comparador;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    public ProductoController() {
        this.productoDAO = new ProductoDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public List<Producto> listar() throws SQLException {
        return productoDAO.listarActivos();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    /** Filtra por nombre o código (búsqueda secuencial) y luego ordena (inserción). */
    public List<Producto> filtrarYOrdenar(List<Producto> productos, String texto, Orden orden) {
        List<Producto> filtrados = Algoritmos.buscarSecuencial(productos, texto,
                p -> p.getNombre() + " " + p.getCodigoBarras());
        return Algoritmos.ordenarPorInsercion(filtrados, orden.getComparador());
    }

    /**
     * Guarda un producto a partir de los textos del formulario.
     * Si idProducto es 0 se da de alta; si no, se modifica el existente.
     */
    public Producto guardar(int idProducto, String codigo, String nombre, Categoria categoria,
                            String costoTxt, String precioTxt, String stockTxt, String minimoTxt)
            throws SQLException, DatosInvalidosException {

        BigDecimal costo = leerImporte(costoTxt, "costo");
        BigDecimal precio = leerImporte(precioTxt, "precio de venta");
        int stock = leerEntero(stockTxt, "stock inicial");
        int minimo = leerEntero(minimoTxt, "stock mínimo");

        if (precio.compareTo(costo) < 0) {
            throw new DatosInvalidosException("El precio de venta no puede ser menor que el costo.");
        }

        Producto producto;
        try {
            // El constructor y los setters validan las reglas del negocio
            // y lanzan IllegalArgumentException si algo no se cumple.
            producto = new Producto(idProducto, codigo, nombre, categoria, costo, precio, stock, minimo, true);
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException(e.getMessage(), e);
        }

        if (idProducto == 0) {
            productoDAO.insertar(producto);
        } else {
            productoDAO.actualizar(producto);
        }
        return producto;
    }

    public void darDeBaja(Producto producto) throws SQLException {
        productoDAO.darDeBaja(producto.getIdProducto());
    }

    /** Convierte un texto como "1500" o "1.500,50" en un importe. */
    private BigDecimal leerImporte(String texto, String campo) throws DatosInvalidosException {
        if (texto == null || texto.isBlank()) {
            throw new DatosInvalidosException("Completá el " + campo + ".");
        }
        // Acepta tanto 1500.50 como el formato argentino 1.500,50
        String normalizado = texto.trim();
        if (normalizado.contains(",")) {
            normalizado = normalizado.replace(".", "").replace(",", ".");
        }
        try {
            BigDecimal valor = new BigDecimal(normalizado);
            if (valor.signum() < 0) {
                throw new DatosInvalidosException("El " + campo + " no puede ser negativo.");
            }
            return valor.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El " + campo + " debe ser un número (por ejemplo, 1500,50).", e);
        }
    }

    private int leerEntero(String texto, String campo) throws DatosInvalidosException {
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor < 0) {
                throw new DatosInvalidosException("El " + campo + " no puede ser negativo.");
            }
            return valor;
        } catch (NumberFormatException | NullPointerException e) {
            throw new DatosInvalidosException("El " + campo + " debe ser un número entero.", e);
        }
    }
}

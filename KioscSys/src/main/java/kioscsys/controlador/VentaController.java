package kioscsys.controlador;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import kioscsys.archivo.ArchivoTexto;
import kioscsys.archivo.TicketVenta;
import kioscsys.dao.ProductoDAO;
import kioscsys.dao.VentaDAO;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.excepciones.StockInsuficienteException;
import kioscsys.modelo.DetalleVenta;
import kioscsys.modelo.Producto;
import kioscsys.modelo.Usuario;
import kioscsys.modelo.Venta;

/**
 * Caso de uso CU-03 Registrar venta (incluye CU-02 Buscar producto).
 * Coordina la pantalla, el modelo y los DAO, siguiendo el diagrama de
 * secuencia de análisis del TP2 (GestorVenta).
 */
public class VentaController {

    private final ProductoDAO productoDAO;
    private final VentaDAO ventaDAO;
    private final Usuario vendedor;
    private Venta ventaActual;

    public VentaController(Usuario vendedor) {
        this.vendedor = vendedor;
        this.productoDAO = new ProductoDAO();
        this.ventaDAO = new VentaDAO();
        iniciarVenta();
    }

    public final void iniciarVenta() {
        ventaActual = new Venta(vendedor);
    }

    /** CU-02: busca un producto por código de barras. */
    public Producto buscarProducto(String codigo) throws SQLException, DatosInvalidosException {
        if (codigo == null || codigo.isBlank()) {
            throw new DatosInvalidosException("Ingresá o escaneá un código de barras.");
        }
        Producto producto = productoDAO.buscarPorCodigo(codigo.trim());
        if (producto == null) {
            throw new DatosInvalidosException("No existe un producto activo con el código " + codigo.trim() + ".");
        }
        return producto;
    }

    /**
     * Valida la cantidad ingresada (prueba de caja blanca PU-01 del TP2).
     * Dos decisiones, tres caminos: cantidad inválida, stock insuficiente u OK.
     */
    public ResultadoValidacion validarCantidad(int cantidad, int stock) {
        if (cantidad <= 0) {
            return ResultadoValidacion.CANTIDAD_INVALIDA;
        }
        if (cantidad > stock) {
            return ResultadoValidacion.STOCK_INSUFICIENTE;
        }
        return ResultadoValidacion.OK;
    }

    /**
     * Agrega un ítem a la venta en curso. La cantidad llega como texto desde
     * la pantalla; si no es un número entero se informa sin cerrar el programa.
     *
     * @return el total actualizado de la venta.
     */
    public BigDecimal agregarItem(String codigo, String cantidadTexto)
            throws SQLException, DatosInvalidosException, StockInsuficienteException {
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto.trim());
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("La cantidad debe ser un número entero (por ejemplo, 2).", e);
        }

        Producto producto = buscarProducto(codigo);

        switch (validarCantidad(cantidad, producto.getStockActual())) {
            case CANTIDAD_INVALIDA:
                throw new DatosInvalidosException("La cantidad debe ser mayor que cero.");
            case STOCK_INSUFICIENTE:
                throw new StockInsuficienteException(producto.getNombre(), producto.getStockActual(), cantidad);
            default:
                // OK: la venta vuelve a controlar el stock sumando lo que ya estaba cargado
                ventaActual.agregarDetalle(producto, cantidad);
        }
        return ventaActual.calcularTotal();
    }

    public BigDecimal quitarItem(int indice) {
        ventaActual.quitarDetalle(indice);
        return ventaActual.calcularTotal();
    }

    /**
     * Confirma y graba la venta.
     * @return los productos que quedaron por debajo del stock mínimo (alerta RF-10).
     */
    public List<Producto> confirmarVenta()
            throws SQLException, DatosInvalidosException, StockInsuficienteException {
        if (ventaActual.estaVacia()) {
            throw new DatosInvalidosException("La venta no tiene productos.");
        }
        ventaDAO.registrar(ventaActual);

        // Se consulta el stock ya actualizado para generar las alertas.
        List<Producto> alertas = new ArrayList<>();
        for (DetalleVenta d : ventaActual.getDetalles()) {
            Producto actualizado = productoDAO.buscarPorCodigo(d.getProducto().getCodigoBarras());
            if (actualizado != null && actualizado.estaBajoMinimo()) {
                alertas.add(actualizado);
            }
        }
        return alertas;
    }

    /**
     * Emite el ticket de la venta recién confirmada en un archivo de texto.
     * Se llama después de confirmarVenta: si falla la escritura del archivo,
     * la venta ya quedó grabada en MySQL y no se deshace; solo se avisa.
     */
    public File emitirTicket() throws IOException {
        if (ventaActual.getIdVenta() == 0) {
            throw new IllegalStateException("La venta todavía no se registró en la base.");
        }
        return new TicketVenta(ventaActual).generar();
    }

    /** Lee un ticket ya emitido para mostrarlo en pantalla. */
    public String leerTicket(File ticket) throws IOException {
        return ArchivoTexto.leer(ticket);
    }

    public void cancelarVenta() {
        iniciarVenta();
    }

    public Venta getVentaActual() {
        return ventaActual;
    }
}

package kioscsys.controlador;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kioscsys.archivo.ReporteDiarioTXT;
import kioscsys.dao.VentaDAO;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.modelo.ItemRanking;
import kioscsys.modelo.LineaVendida;
import kioscsys.modelo.ReporteDiario;
import kioscsys.util.Algoritmos;

/**
 * Caso de uso CU-10 Consultar reportes de ventas (versión del prototipo:
 * reporte de un día). Consulta la base mediante VentaDAO, arma el
 * ReporteDiario y lo exporta a archivo.
 */
public class ReporteController {

    private static final DateTimeFormatter FORMATO_DIA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final VentaDAO ventaDAO;
    private final ValorizacionController valorizacion;

    public ReporteController() {
        this.ventaDAO = new VentaDAO();
        this.valorizacion = new ValorizacionController();
    }

    /**
     * Convierte el texto ingresado (dd/mm/aaaa) en una fecha. Si el formato
     * es incorrecto o la fecha no existe (31/02/2026), Java lanza
     * DateTimeParseException, que se traduce a un mensaje del negocio.
     */
    public LocalDate interpretarFecha(String texto) throws DatosInvalidosException {
        try {
            LocalDate dia = LocalDate.parse(texto.trim(), FORMATO_DIA);
            if (dia.isAfter(LocalDate.now())) {
                throw new DatosInvalidosException("No se puede consultar un día futuro.");
            }
            return dia;
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("La fecha debe tener el formato dd/mm/aaaa (por ejemplo, 06/11/2026).", e);
        }
    }

    public String formatear(LocalDate dia) {
        return dia.format(FORMATO_DIA);
    }

    /** Genera el reporte de un día a partir de las ventas guardadas en MySQL. */
    public ReporteDiario generar(LocalDate dia) throws SQLException {
        List<LineaVendida> lineas = ventaDAO.listarLineasDelDia(dia);
        ReporteDiario reporte = new ReporteDiario(dia, lineas);

        // 1) Franjas horarias: la hora de cada línea es el índice del arreglo.
        //    Las líneas vienen ordenadas por venta, así que un ticket nuevo
        //    empieza cada vez que cambia el id.
        int ventaAnterior = -1;
        for (LineaVendida l : lineas) {
            if (l.getIdVenta() != ventaAnterior) {
                reporte.sumarTicket(l.getHora());
                ventaAnterior = l.getIdVenta();
            }
            reporte.sumarImporte(l.getHora(), l.calcularValorVenta());
        }

        // 2) Totales: se reutiliza el cálculo que recibe cualquier lista de
        //    Valorizable (el mismo que valoriza el inventario).
        BigDecimal[] totales = valorizacion.calcularTotales(lineas);
        reporte.setTotales(totales[0], totales[1]);

        // 3) Ranking: un ArrayList que crece con cada producto distinto.
        reporte.setRanking(armarRanking(lineas));
        return reporte;
    }

    /** Agrupa las líneas por producto y las ordena por importe, de mayor a menor. */
    List<ItemRanking> armarRanking(List<LineaVendida> lineas) {
        List<ItemRanking> ranking = new ArrayList<>();
        for (LineaVendida l : lineas) {
            ItemRanking item = buscar(ranking, l.getProducto());
            if (item == null) {
                item = new ItemRanking(l.getProducto());
                ranking.add(item);
            }
            item.acumular(l);
        }
        Comparator<ItemRanking> porImporte = Comparator.comparing(ItemRanking::getImporte).reversed();
        return Algoritmos.ordenarPorInsercion(ranking, porImporte);
    }

    /** Búsqueda secuencial de un producto dentro del ranking. */
    private ItemRanking buscar(List<ItemRanking> ranking, String producto) {
        for (ItemRanking item : ranking) {
            if (item.getProducto().equals(producto)) {
                return item;
            }
        }
        return null;
    }

    /** Guarda el reporte en un archivo de texto y devuelve dónde quedó. */
    public File exportar(ReporteDiario reporte) throws IOException {
        return new ReporteDiarioTXT(reporte).generar();
    }
}

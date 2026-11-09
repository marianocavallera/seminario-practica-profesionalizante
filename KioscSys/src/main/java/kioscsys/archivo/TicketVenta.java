package kioscsys.archivo;

import java.io.File;
import java.io.PrintWriter;
import kioscsys.modelo.DetalleVenta;
import kioscsys.modelo.Venta;
import kioscsys.util.Formato;

/**
 * Comprobante de una venta en formato texto (tickets/ticket-000012.txt).
 * Simula el ticket que se le entrega al cliente; no es una factura fiscal.
 */
public class TicketVenta extends ArchivoTexto {

    /** Carpeta donde se guardan los tickets, junto al programa. */
    public static final File CARPETA = new File("tickets");
    private static final int ANCHO = 40;

    private final Venta venta;

    public TicketVenta(Venta venta) {
        super(CARPETA);
        this.venta = venta;
    }

    @Override
    protected String nombreArchivo() {
        return String.format("ticket-%06d.txt", venta.getIdVenta());
    }

    @Override
    protected void escribirContenido(PrintWriter salida) {
        String linea = "-".repeat(ANCHO);
        salida.println(centrar("KIOSCO DON MATEO"));
        salida.println(centrar("Comprobante no válido como factura"));
        salida.println(linea);
        salida.printf("Ticket N.º %06d%n", venta.getIdVenta());
        salida.println("Fecha:    " + Formato.fechaHora(venta.getFecha()));
        salida.println("Atendió:  " + venta.getUsuario().getNombreCompleto());
        salida.println(linea);
        for (DetalleVenta d : venta.getDetalles()) {
            salida.println(recortar(d.getProducto().getNombre(), ANCHO));
            salida.printf("  %3d x %-14s %17s%n", d.getCantidad(),
                    Formato.moneda(d.getPrecioUnitario()), Formato.moneda(d.calcularSubtotal()));
        }
        salida.println(linea);
        salida.printf("%-12s %27s%n", "TOTAL", Formato.moneda(venta.calcularTotal()));
        salida.printf("Unidades: %d%n", venta.getCantidadUnidades());
        salida.println(linea);
        salida.println(centrar("¡Gracias por su compra!"));
    }

    private static String centrar(String texto) {
        int margen = Math.max(0, (ANCHO - texto.length()) / 2);
        return " ".repeat(margen) + texto;
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
}

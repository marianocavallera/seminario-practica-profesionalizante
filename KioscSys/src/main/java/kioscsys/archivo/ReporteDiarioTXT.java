package kioscsys.archivo;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import kioscsys.modelo.ItemRanking;
import kioscsys.modelo.ReporteDiario;
import kioscsys.util.Formato;

/**
 * Reporte de ventas del día en texto (exportaciones/reporte-ventas-2026-11-06.txt),
 * para que el dueño lo guarde o lo imprima al cerrar la caja.
 */
public class ReporteDiarioTXT extends ArchivoTexto {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TOP = 5;

    private final ReporteDiario reporte;

    public ReporteDiarioTXT(ReporteDiario reporte) {
        super(ExportacionValorizacionCSV.CARPETA);
        this.reporte = reporte;
    }

    @Override
    protected String nombreArchivo() {
        return "reporte-ventas-" + reporte.getDia() + ".txt";
    }

    @Override
    protected void escribirContenido(PrintWriter salida) {
        String linea = "=".repeat(56);
        salida.println(linea);
        salida.println("KIOSCO DON MATEO - Reporte de ventas del " + reporte.getDia().format(DIA));
        salida.println(linea);
        salida.printf("Tickets emitidos:   %d%n", reporte.getCantidadTickets());
        salida.printf("Total vendido:      %s%n", Formato.moneda(reporte.getTotalVendido()));
        salida.printf("Ganancia bruta:     %s%n", Formato.moneda(reporte.getGanancia()));
        salida.printf("Ticket promedio:    %s%n", Formato.moneda(reporte.getTicketPromedio()));
        int pico = reporte.getHoraPico();
        salida.println("Hora pico:          " + (pico < 0 ? "-" : String.format("%02d a %02d h", pico, pico + 1)));
        salida.println();

        // Recorrido de los arreglos por índice: la posición es la hora
        salida.println("VENTAS POR FRANJA HORARIA");
        salida.printf("%-12s %8s %16s%n", "Franja", "Tickets", "Importe");
        int[] tickets = reporte.getTicketsPorHora();
        BigDecimal[] importes = reporte.getImportePorHora();
        for (int h = 0; h < tickets.length; h++) {
            if (tickets[h] > 0) {
                salida.printf("%02d a %02d h    %8d %16s%n", h, h + 1, tickets[h], Formato.moneda(importes[h]));
            }
        }
        salida.println();

        // Arreglo de tamaño fijo con los más vendidos
        ItemRanking[] top = reporte.getTop(TOP);
        salida.println("TOP " + TOP + " PRODUCTOS POR IMPORTE");
        for (int i = 0; i < top.length; i++) {
            salida.printf("%d. %-28s %4d u. %14s%n", i + 1, top[i].getProducto(),
                    top[i].getUnidades(), Formato.moneda(top[i].getImporte()));
        }
        if (top.length == 0) {
            salida.println("Sin ventas registradas.");
        }
        salida.println(linea);
    }
}

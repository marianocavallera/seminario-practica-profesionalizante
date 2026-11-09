package kioscsys.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Resumen de las ventas de un día (CU-10 Consultar reportes de ventas).
 *
 * Combina las dos estructuras de datos según lo que representa cada dato:
 *
 *  - ARREGLOS para las franjas horarias: un día tiene siempre 24 horas, así
 *    que el tamaño se conoce de antemano y no cambia. La hora de la venta
 *    se usa directamente como índice (ticketsPorHora[18] son los tickets de
 *    18 a 19 h), sin buscar nada.
 *
 *  - ARRAYLIST para las líneas vendidas y el ranking de productos: la
 *    cantidad depende de lo que se vendió ese día y no se conoce hasta
 *    consultar la base, por lo que hace falta una colección que crezca.
 */
public class ReporteDiario {

    /** Cantidad de franjas horarias de un día. */
    public static final int HORAS = 24;
    /** Horario de atención del kiosco: es el tramo que se muestra en el gráfico. */
    public static final int HORA_APERTURA = 7;
    public static final int HORA_CIERRE = 23;

    private final LocalDate dia;

    // Arreglos: tamaño fijo, declarados e inicializados al crear el reporte
    private final int[] ticketsPorHora = new int[HORAS];            // se inicializa en 0
    private final BigDecimal[] importePorHora = new BigDecimal[HORAS]; // se inicializa en null...

    // Colecciones dinámicas
    private final List<LineaVendida> lineas;
    private final List<ItemRanking> ranking = new ArrayList<>();

    private BigDecimal totalVendido = BigDecimal.ZERO;
    private BigDecimal totalCosto = BigDecimal.ZERO;
    private int cantidadTickets;

    public ReporteDiario(LocalDate dia, List<LineaVendida> lineas) {
        this.dia = dia;
        this.lineas = new ArrayList<>(lineas);
        Arrays.fill(importePorHora, BigDecimal.ZERO);  // ...por eso se completa con ceros
    }

    /** Suma un ticket en la franja de la hora indicada. */
    public void sumarTicket(int hora) {
        ticketsPorHora[hora]++;
        cantidadTickets++;
    }

    /** Suma un importe en la franja de la hora indicada. */
    public void sumarImporte(int hora, BigDecimal importe) {
        importePorHora[hora] = importePorHora[hora].add(importe);
    }

    public void setTotales(BigDecimal totalCosto, BigDecimal totalVendido) {
        this.totalCosto = totalCosto;
        this.totalVendido = totalVendido;
    }

    public void setRanking(List<ItemRanking> ordenado) {
        ranking.clear();
        ranking.addAll(ordenado);
    }

    /**
     * Hora con mayor facturación, o -1 si no hubo ventas.
     * Recorrido de un arreglo buscando el máximo.
     */
    public int getHoraPico() {
        int pico = -1;
        BigDecimal maximo = BigDecimal.ZERO;
        for (int h = 0; h < HORAS; h++) {
            if (importePorHora[h].compareTo(maximo) > 0) {
                maximo = importePorHora[h];
                pico = h;
            }
        }
        return pico;
    }

    /**
     * Los primeros n productos del ranking como ARREGLO de tamaño fijo
     * (por ejemplo, el "Top 5" que se imprime en el reporte en archivo).
     * Si se vendieron menos de n productos, el arreglo es más corto.
     */
    public ItemRanking[] getTop(int n) {
        int tamanio = Math.min(n, ranking.size());
        ItemRanking[] top = new ItemRanking[tamanio];
        for (int i = 0; i < tamanio; i++) {
            top[i] = ranking.get(i);
        }
        return top;
    }

    /** Copias de los arreglos: quien los recibe no puede modificar el reporte. */
    public int[] getTicketsPorHora() {
        return Arrays.copyOf(ticketsPorHora, HORAS);
    }

    public BigDecimal[] getImportePorHora() {
        return Arrays.copyOf(importePorHora, HORAS);
    }

    public List<LineaVendida> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public List<ItemRanking> getRanking() {
        return Collections.unmodifiableList(ranking);
    }

    public LocalDate getDia() {
        return dia;
    }

    public int getCantidadTickets() {
        return cantidadTickets;
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    public BigDecimal getGanancia() {
        return totalVendido.subtract(totalCosto);
    }

    public BigDecimal getTicketPromedio() {
        if (cantidadTickets == 0) {
            return BigDecimal.ZERO;
        }
        return totalVendido.divide(BigDecimal.valueOf(cantidadTickets), 2, java.math.RoundingMode.HALF_UP);
    }

    public boolean estaVacio() {
        return lineas.isEmpty();
    }
}

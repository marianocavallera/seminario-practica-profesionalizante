package kioscsys.archivo;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import kioscsys.modelo.Producto;

/**
 * Exporta la valorización del inventario a un archivo CSV
 * (exportaciones/valorizacion-2026-11-06.csv), para enviársela al contador
 * o abrirla en una planilla de cálculo.
 *
 * Se usa punto y coma como separador y coma decimal porque es lo que
 * espera Excel con la configuración regional de Argentina.
 */
public class ExportacionValorizacionCSV extends ArchivoTexto {

    public static final File CARPETA = new File("exportaciones");

    // Arreglo con los títulos de las columnas, inicializado en la declaración
    private static final String[] COLUMNAS = {
        "Código", "Producto", "Categoría", "Stock", "Stock mínimo",
        "Costo unitario", "Precio de venta", "Valor a costo", "Valor a venta", "Margen", "Bajo mínimo"
    };
    private static final char SEPARADOR = ';';

    private final List<Producto> productos;
    private final LocalDate fecha;

    public ExportacionValorizacionCSV(List<Producto> productos) {
        super(CARPETA);
        this.productos = productos;
        this.fecha = LocalDate.now();
    }

    @Override
    protected String nombreArchivo() {
        return "valorizacion-" + fecha + ".csv";
    }

    @Override
    protected void escribirContenido(PrintWriter salida) {
        salida.print('﻿');   // marca BOM: Excel reconoce UTF-8 y muestra bien las tildes
        salida.println(unir(COLUMNAS));

        for (Producto p : productos) {
            // Un arreglo por fila, del mismo largo que COLUMNAS
            String[] fila = new String[COLUMNAS.length];
            fila[0] = p.getCodigoBarras();
            fila[1] = p.getNombre();
            fila[2] = p.getCategoria().getNombre();
            fila[3] = String.valueOf(p.getStockActual());
            fila[4] = String.valueOf(p.getStockMinimo());
            fila[5] = numero(p.getCostoUnitario());
            fila[6] = numero(p.getPrecioVenta());
            fila[7] = numero(p.calcularValorCosto());
            fila[8] = numero(p.calcularValorVenta());
            fila[9] = numero(p.calcularMargen());
            fila[10] = p.estaBajoMinimo() ? "Sí" : "No";
            salida.println(unir(fila));
        }
    }

    /** Une los valores de un arreglo con el separador, escapando los que lo contienen. */
    private static String unir(String[] valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                sb.append(SEPARADOR);
            }
            String v = valores[i];
            if (v.indexOf(SEPARADOR) >= 0 || v.indexOf('"') >= 0) {
                v = '"' + v.replace("\"", "\"\"") + '"';
            }
            sb.append(v);
        }
        return sb.toString();
    }

    private static String numero(BigDecimal importe) {
        return importe.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}

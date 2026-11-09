package kioscsys.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import kioscsys.controlador.ReporteController;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.modelo.ItemRanking;
import kioscsys.modelo.ReporteDiario;
import kioscsys.util.Formato;

/**
 * Pantalla del reporte de ventas del día (CU-10). Solo muestra datos y
 * captura acciones: la consulta y los cálculos los hace ReporteController.
 */
public class ReportePanel extends JPanel implements Recargable {

    private final ReporteController controller = new ReporteController();
    private final JTextField txtDia = new JTextField(10);
    private final GraficoBarras grafico = new GraficoBarras();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"#", "Producto", "Unidades", "Importe", "Ganancia"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    // Arreglo de etiquetas para los indicadores del pie de la pantalla
    private final JLabel[] indicadores = new JLabel[5];
    private ReporteDiario reporte;

    public ReportePanel() {
        super(new BorderLayout(10, 10));
        setBorder(Estilo.margen(16));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Día (dd/mm/aaaa):"));
        filtros.add(txtDia);
        JButton btnConsultar = Estilo.boton("Consultar");
        JButton btnHoy = Estilo.boton("Hoy");
        JButton btnExportar = Estilo.boton("Exportar a TXT");
        filtros.add(btnConsultar);
        filtros.add(btnHoy);
        filtros.add(btnExportar);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.add(Estilo.titulo("Reporte de ventas del día"), BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        Estilo.prepararTabla(tabla);
        int[] anchos = {30, 210, 70, 110, 110};   // ancho de cada columna, por posición
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(javax.swing.BorderFactory.createTitledBorder("Productos más vendidos"));
        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grafico, scrollTabla);
        centro.setResizeWeight(0.5);
        centro.setBorder(null);
        add(centro, BorderLayout.CENTER);

        JPanel pie = new JPanel(new GridLayout(1, indicadores.length, 12, 0));
        for (int i = 0; i < indicadores.length; i++) {
            indicadores[i] = new JLabel();
            indicadores[i].setFont(Estilo.NORMAL.deriveFont(Font.BOLD, 15f));
            pie.add(indicadores[i]);
        }
        add(pie, BorderLayout.SOUTH);

        txtDia.setText(controller.formatear(LocalDate.now()));
        txtDia.addActionListener(e -> consultar());
        btnConsultar.addActionListener(e -> consultar());
        btnHoy.addActionListener(e -> {
            txtDia.setText(controller.formatear(LocalDate.now()));
            consultar();
        });
        btnExportar.addActionListener(e -> exportar());
    }

    @Override
    public void recargar() {
        consultar();
    }

    private void consultar() {
        try {
            LocalDate dia = controller.interpretarFecha(txtDia.getText());
            reporte = controller.generar(dia);
            mostrar();
        } catch (DatosInvalidosException e) {
            Estilo.aviso(this, e.getMessage());
            txtDia.requestFocus();
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
        }
    }

    private void mostrar() {
        grafico.setDatos(reporte.getImportePorHora(), ReporteDiario.HORA_APERTURA,
                ReporteDiario.HORA_CIERRE, reporte.getHoraPico());

        modelo.setRowCount(0);
        int posicion = 1;
        for (ItemRanking item : reporte.getRanking()) {
            modelo.addRow(new Object[]{posicion++, item.getProducto(), item.getUnidades(),
                Formato.moneda(item.getImporte()), Formato.moneda(item.getGanancia())});
        }

        int pico = reporte.getHoraPico();
        String[] textos = {
            "Tickets: " + reporte.getCantidadTickets(),
            "Vendido: " + Formato.moneda(reporte.getTotalVendido()),
            "Ganancia: " + Formato.moneda(reporte.getGanancia()),
            "Promedio: " + Formato.moneda(reporte.getTicketPromedio()),
            "Hora pico: " + (pico < 0 ? "-" : String.format("%02d a %02d h", pico, pico + 1))
        };
        for (int i = 0; i < indicadores.length; i++) {
            indicadores[i].setText(textos[i]);
        }
        if (reporte.estaVacio()) {
            indicadores[0].setText("Sin ventas ese día.");
        }
    }

    private void exportar() {
        if (reporte == null) {
            Estilo.aviso(this, "Primero consultá un día.");
            return;
        }
        try {
            File archivo = controller.exportar(reporte);
            Estilo.info(this, "Reporte guardado en:\n" + archivo.getAbsolutePath());
        } catch (IOException e) {
            Estilo.errorArchivo(this, "guardar el reporte", e);
        }
    }
}

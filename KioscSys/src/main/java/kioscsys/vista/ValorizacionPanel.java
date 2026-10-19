package kioscsys.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kioscsys.controlador.ProductoController;
import kioscsys.controlador.ValorizacionController;
import kioscsys.modelo.Categoria;
import kioscsys.modelo.Producto;
import kioscsys.util.Formato;

/**
 * Pantalla de valorización del inventario (CU-09) con alertas de stock
 * mínimo (CU-11). Prototipo de la Figura 11 del TP2: los productos bajo el
 * mínimo se pintan en rojo.
 */
public class ValorizacionPanel extends JPanel implements Recargable {

    private static final String TODAS = "Todas";

    private final ValorizacionController controller = new ValorizacionController();
    private final ProductoController productoController = new ProductoController();
    private final JComboBox<Object> cmbCategoria = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Producto", "Categoría", "Stock", "Mínimo", "Valor a costo", "Valor a venta", "Margen"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblCosto = new JLabel();
    private final JLabel lblVenta = new JLabel();
    private final JLabel lblMargen = new JLabel();
    private final JLabel lblAlertas = new JLabel();
    private List<Producto> mostrados = new ArrayList<>();

    public ValorizacionPanel() {
        super(new BorderLayout(10, 10));
        setBorder(Estilo.margen(16));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Categoría:"));
        filtros.add(cmbCategoria);
        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.add(Estilo.titulo("Valorización de inventario"), BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        Estilo.prepararTabla(tabla);
        tabla.setDefaultRenderer(Object.class, new RenderAlerta());
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel totales = new JPanel(new GridLayout(2, 3, 16, 4));
        for (JLabel l : new JLabel[]{lblCosto, lblVenta, lblMargen}) {
            l.setFont(Estilo.NORMAL.deriveFont(Font.BOLD, 16f));
            totales.add(l);
        }
        lblAlertas.setForeground(Estilo.TEXTO_ALERTA);
        lblAlertas.setFont(Estilo.NORMAL);
        totales.add(lblAlertas);
        add(totales, BorderLayout.SOUTH);

        cmbCategoria.addActionListener(e -> mostrar());
    }

    @Override
    public void recargar() {
        try {
            Object seleccion = cmbCategoria.getSelectedItem();
            cmbCategoria.removeAllItems();
            cmbCategoria.addItem(TODAS);
            for (Categoria c : productoController.listarCategorias()) {
                cmbCategoria.addItem(c);
            }
            cmbCategoria.setSelectedItem(seleccion != null ? seleccion : TODAS);
            mostrar();
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
        }
    }

    private void mostrar() {
        Object seleccion = cmbCategoria.getSelectedItem();
        if (seleccion == null) {
            return;   // el combo se está recargando
        }
        Categoria categoria = (seleccion instanceof Categoria c) ? c : null;
        try {
            mostrados = controller.listar(categoria);
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
            return;
        }

        modelo.setRowCount(0);
        for (Producto p : mostrados) {
            modelo.addRow(new Object[]{
                p.getNombre(), p.getCategoria().getNombre(), p.getStockActual(), p.getStockMinimo(),
                Formato.moneda(p.calcularValorCosto()), Formato.moneda(p.calcularValorVenta()),
                Formato.moneda(p.calcularMargen())
            });
        }

        BigDecimal[] t = controller.calcularTotales(mostrados);
        lblCosto.setText("Total a costo: " + Formato.moneda(t[0]));
        lblVenta.setText("Total a venta: " + Formato.moneda(t[1]));
        lblMargen.setText("Margen potencial: " + Formato.moneda(t[2]));

        int alertas = controller.alertas(mostrados).size();
        lblAlertas.setText(alertas == 0 ? "Sin productos bajo el stock mínimo."
                : alertas + " producto(s) bajo el stock mínimo (en rojo).");
    }

    /** Pinta en rojo las filas de productos bajo el stock mínimo. */
    private class RenderAlerta extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                       boolean foco, int fila, int col) {
            Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
            if (!sel) {
                boolean alerta = fila < mostrados.size() && mostrados.get(fila).estaBajoMinimo();
                c.setBackground(alerta ? Estilo.FONDO_ALERTA : t.getBackground());
                c.setForeground(alerta ? Estilo.TEXTO_ALERTA : t.getForeground());
            }
            return c;
        }
    }
}

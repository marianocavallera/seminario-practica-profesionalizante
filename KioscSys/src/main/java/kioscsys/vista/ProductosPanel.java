package kioscsys.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import kioscsys.controlador.ProductoController;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.modelo.Categoria;
import kioscsys.modelo.Producto;
import kioscsys.util.Formato;

/**
 * Pantalla Gestionar productos (CU-05): listado con búsqueda y orden,
 * más alta, modificación y baja.
 */
public class ProductosPanel extends JPanel implements Recargable {

    private final ProductoController controller = new ProductoController();
    private final JTextField txtBuscar = new JTextField(20);
    private final JComboBox<ProductoController.Orden> cmbOrden =
            new JComboBox<>(ProductoController.Orden.values());
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Código", "Nombre", "Categoría", "Costo", "Precio", "Margen unit.", "Stock", "Mínimo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private List<Producto> todos = new ArrayList<>();      // cargados de la base
    private List<Producto> visibles = new ArrayList<>();   // filtrados y ordenados

    public ProductosPanel() {
        super(new BorderLayout(10, 10));
        setBorder(Estilo.margen(16));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Ordenar por:"));
        filtros.add(cmbOrden);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.add(Estilo.titulo("Gestionar productos"), BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        Estilo.prepararTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnNuevo = Estilo.boton("Nuevo");
        JButton btnEditar = Estilo.boton("Modificar");
        JButton btnBaja = Estilo.boton("Dar de baja");
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnBaja);
        add(acciones, BorderLayout.SOUTH);

        // El filtro se aplica mientras se escribe, sin volver a consultar la base
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refrescarTabla(); }
            @Override public void removeUpdate(DocumentEvent e) { refrescarTabla(); }
            @Override public void changedUpdate(DocumentEvent e) { refrescarTabla(); }
        });
        cmbOrden.addActionListener(e -> refrescarTabla());
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> {
            Producto p = seleccionado();
            if (p != null) {
                abrirFormulario(p);
            }
        });
        btnBaja.addActionListener(e -> darDeBaja());
    }

    @Override
    public void recargar() {
        try {
            todos = controller.listar();
            refrescarTabla();
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
        }
    }

    private void refrescarTabla() {
        ProductoController.Orden orden = (ProductoController.Orden) cmbOrden.getSelectedItem();
        visibles = controller.filtrarYOrdenar(todos, txtBuscar.getText(), orden);
        modelo.setRowCount(0);
        for (Producto p : visibles) {
            modelo.addRow(new Object[]{
                p.getCodigoBarras(), p.getNombre(), p.getCategoria().getNombre(),
                Formato.moneda(p.getCostoUnitario()), Formato.moneda(p.getPrecioVenta()),
                Formato.moneda(p.calcularMargenUnitario()), p.getStockActual(), p.getStockMinimo()
            });
        }
    }

    private Producto seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccioná un producto de la tabla.");
            return null;
        }
        return visibles.get(fila);
    }

    /** Formulario de alta (producto == null) o modificación. */
    private void abrirFormulario(Producto producto) {
        List<Categoria> categorias;
        try {
            categorias = controller.listarCategorias();
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
            return;
        }
        boolean esNuevo = (producto == null);

        JTextField txtCodigo = new JTextField(esNuevo ? "" : producto.getCodigoBarras());
        JTextField txtNombre = new JTextField(esNuevo ? "" : producto.getNombre());
        JComboBox<Categoria> cmbCategoria = new JComboBox<>(categorias.toArray(new Categoria[0]));
        JTextField txtCosto = new JTextField(esNuevo ? "" : producto.getCostoUnitario().toPlainString());
        JTextField txtPrecio = new JTextField(esNuevo ? "" : producto.getPrecioVenta().toPlainString());
        JTextField txtStock = new JTextField(esNuevo ? "0" : String.valueOf(producto.getStockActual()));
        JTextField txtMinimo = new JTextField(esNuevo ? "0" : String.valueOf(producto.getStockMinimo()));
        if (!esNuevo) {
            cmbCategoria.setSelectedItem(producto.getCategoria());
            // El stock de un producto existente solo cambia por compras, ventas o ajustes
            txtStock.setEnabled(false);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Código de barras:")); form.add(txtCodigo);
        form.add(new JLabel("Nombre:"));           form.add(txtNombre);
        form.add(new JLabel("Categoría:"));        form.add(cmbCategoria);
        form.add(new JLabel("Costo unitario:"));   form.add(txtCosto);
        form.add(new JLabel("Precio de venta:"));  form.add(txtPrecio);
        form.add(new JLabel("Stock inicial:"));    form.add(txtStock);
        form.add(new JLabel("Stock mínimo:"));     form.add(txtMinimo);

        String titulo = esNuevo ? "Nuevo producto" : "Modificar producto";
        // Se repite el formulario hasta que los datos sean válidos o el usuario cancele
        while (true) {
            int opcion = JOptionPane.showConfirmDialog(this, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                controller.guardar(esNuevo ? 0 : producto.getIdProducto(),
                        txtCodigo.getText(), txtNombre.getText(),
                        (Categoria) cmbCategoria.getSelectedItem(),
                        txtCosto.getText(), txtPrecio.getText(),
                        txtStock.getText(), txtMinimo.getText());
                Estilo.info(this, esNuevo ? "Producto creado." : "Producto actualizado.");
                recargar();
                return;
            } catch (DatosInvalidosException e) {
                Estilo.aviso(this, e.getMessage());   // vuelve a mostrar el formulario
            } catch (SQLException e) {
                Estilo.errorBD(this, e);
                return;
            }
        }
    }

    private void darDeBaja() {
        Producto p = seleccionado();
        if (p == null) {
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja \"" + p.getNombre() + "\"?\nDejará de aparecer para la venta, "
                + "pero se conserva en el historial.", "Dar de baja", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            try {
                controller.darDeBaja(p);
                recargar();
            } catch (SQLException e) {
                Estilo.errorBD(this, e);
            }
        }
    }
}

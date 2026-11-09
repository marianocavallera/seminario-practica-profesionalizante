package kioscsys.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import kioscsys.controlador.VentaController;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.excepciones.StockInsuficienteException;
import kioscsys.modelo.DetalleVenta;
import kioscsys.modelo.Producto;
import kioscsys.modelo.Usuario;
import kioscsys.util.Formato;

/**
 * Pantalla Registrar venta (CU-03, prototipo de la Figura 10 del TP2).
 * Pensada para el mostrador: el foco vuelve siempre al código de barras,
 * Enter agrega, F10 confirma y Escape cancela (RNF-03).
 */
public class VentaPanel extends JPanel implements Recargable {

    private final VentaController controller;
    private final JTextField txtCodigo = new JTextField(16);
    private final JTextField txtCantidad = new JTextField("1", 4);
    private final JLabel lblTotal = new JLabel(Formato.moneda(BigDecimal.ZERO), SwingConstants.RIGHT);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Cantidad", "Precio unit.", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public VentaPanel(Usuario vendedor) {
        super(new BorderLayout(10, 10));
        this.controller = new VentaController(vendedor);
        setBorder(Estilo.margen(16));

        // --- Cabecera: título y carga de productos ---
        JPanel carga = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        carga.add(new JLabel("Código de barras:"));
        carga.add(txtCodigo);
        carga.add(new JLabel("Cantidad:"));
        carga.add(txtCantidad);
        JButton btnAgregar = Estilo.boton("Agregar (Enter)");
        carga.add(btnAgregar);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.add(Estilo.titulo("Registrar venta"), BorderLayout.NORTH);
        norte.add(carga, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // --- Centro: ítems cargados ---
        Estilo.prepararTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Sur: total y acciones ---
        lblTotal.setFont(Estilo.TOTAL);
        lblTotal.setForeground(Estilo.PRIMARIO);
        JButton btnQuitar = Estilo.boton("Quitar ítem");
        JButton btnCancelar = Estilo.boton("Cancelar (Esc)");
        JButton btnConfirmar = Estilo.boton("Confirmar (F10)");
        btnConfirmar.setPreferredSize(new Dimension(180, 44));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.add(btnQuitar);
        acciones.add(btnCancelar);
        acciones.add(btnConfirmar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(acciones, BorderLayout.WEST);
        JPanel total = new JPanel(new BorderLayout());
        JLabel etiqueta = new JLabel("TOTAL  ");
        etiqueta.setFont(Estilo.TOTAL);
        total.add(etiqueta, BorderLayout.WEST);
        total.add(lblTotal, BorderLayout.CENTER);
        sur.add(total, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);

        // --- Eventos ---
        txtCodigo.addActionListener(e -> txtCantidad.requestFocus());
        txtCantidad.addActionListener(e -> agregar());
        btnAgregar.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitar());
        btnCancelar.addActionListener(e -> cancelar());
        btnConfirmar.addActionListener(e -> confirmar());
        atajo("F10", "confirmar", this::confirmar);
        atajo("ESCAPE", "cancelar", this::cancelar);
    }

    private void atajo(String tecla, String nombre, Runnable accion) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nombre);
        getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accion.run();
            }
        });
    }

    private void agregar() {
        try {
            BigDecimal total = controller.agregarItem(txtCodigo.getText(), txtCantidad.getText());
            actualizarTabla(total);
            txtCodigo.setText("");
            txtCantidad.setText("1");
        } catch (DatosInvalidosException | StockInsuficienteException e) {
            // Errores del usuario: se avisa y se puede seguir vendiendo
            Estilo.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
        } finally {
            txtCodigo.requestFocus();
        }
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccioná en la tabla el ítem que querés quitar.");
            return;
        }
        actualizarTabla(controller.quitarItem(fila));
    }

    private void cancelar() {
        if (controller.getVentaActual().estaVacia()) {
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this, "¿Descartar la venta en curso?",
                "Cancelar venta", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            controller.cancelarVenta();
            actualizarTabla(BigDecimal.ZERO);
        }
    }

    private void confirmar() {
        BigDecimal total = controller.getVentaActual().calcularTotal();
        try {
            List<Producto> alertas = controller.confirmarVenta();
            StringBuilder msg = new StringBuilder("Venta registrada. Total cobrado: " + Formato.moneda(total));
            if (!alertas.isEmpty()) {
                msg.append("\n\nAtención: quedaron por debajo del stock mínimo:");
                for (Producto p : alertas) {
                    msg.append("\n  • ").append(p.getNombre())
                       .append(" (quedan ").append(p.getStockActual())
                       .append(", mínimo ").append(p.getStockMinimo()).append(")");
                }
            }
            mostrarResultado(msg);
            controller.iniciarVenta();
            actualizarTabla(BigDecimal.ZERO);
        } catch (DatosInvalidosException | StockInsuficienteException e) {
            Estilo.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
        } finally {
            txtCodigo.requestFocus();
        }
    }

    /**
     * Emite el ticket en archivo y muestra el resultado. Un error de archivo
     * no anula la venta: ya quedó registrada en la base.
     */
    private void mostrarResultado(StringBuilder msg) {
        File ticket = null;
        try {
            ticket = controller.emitirTicket();
            msg.append("\n\nTicket guardado en: ").append(ticket.getPath());
        } catch (IOException e) {
            msg.append("\n\nLa venta se registró, pero no se pudo guardar el ticket:\n").append(e.getMessage());
        }
        if (ticket == null) {
            Estilo.aviso(this, msg.toString());
            return;
        }
        Object[] opciones = {"Aceptar", "Ver ticket"};
        int eleccion = JOptionPane.showOptionDialog(this, msg.toString(), "KioscSys",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);
        if (eleccion == 1) {
            try {
                Estilo.mostrarTexto(this, "Ticket " + ticket.getName(), controller.leerTicket(ticket));
            } catch (IOException e) {
                Estilo.errorArchivo(this, "leer el ticket", e);
            }
        }
    }

    private void actualizarTabla(BigDecimal total) {
        modelo.setRowCount(0);
        for (DetalleVenta d : controller.getVentaActual().getDetalles()) {
            modelo.addRow(new Object[]{
                d.getProducto().getCodigoBarras(),
                d.getProducto().getNombre(),
                d.getCantidad(),
                Formato.moneda(d.getPrecioUnitario()),
                Formato.moneda(d.calcularSubtotal())
            });
        }
        lblTotal.setText(Formato.moneda(total));
    }

    @Override
    public void recargar() {
        txtCodigo.requestFocusInWindow();
    }
}

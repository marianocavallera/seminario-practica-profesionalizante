package kioscsys.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.Border;

/** Colores, fuentes y mensajes comunes a todas las pantallas. */
public final class Estilo {

    public static final Color PRIMARIO = new Color(0x1F3864);
    public static final Color FONDO_ALERTA = new Color(0xFDE2E1);
    public static final Color TEXTO_ALERTA = new Color(0x9C1C1C);
    public static final Color FONDO_PANEL = new Color(0xF4F6FA);

    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font TOTAL = new Font("SansSerif", Font.BOLD, 28);

    private Estilo() {
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TITULO);
        l.setForeground(PRIMARIO);
        return l;
    }

    public static Border margen(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(NORMAL);
        b.setFocusPainted(false);
        return b;
    }

    public static void prepararTabla(JTable tabla) {
        tabla.setFont(NORMAL);
        tabla.setRowHeight(26);
        tabla.getTableHeader().setFont(NORMAL.deriveFont(Font.BOLD));
        tabla.setAutoCreateRowSorter(false);
        tabla.setFillsViewportHeight(true);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "KioscSys", JOptionPane.ERROR_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "KioscSys", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "KioscSys", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Mensaje para errores de base de datos: no se muestra el detalle técnico al vendedor. */
    public static void errorBD(Component padre, Exception e) {
        System.err.println("Error de base de datos: " + e.getMessage());
        error(padre, "No se pudo acceder a la base de datos.\n"
                + "Verificá que MySQL esté iniciado e intentá de nuevo.\n\nDetalle: " + e.getMessage());
    }
}

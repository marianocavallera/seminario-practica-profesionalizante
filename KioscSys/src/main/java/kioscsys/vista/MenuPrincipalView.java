package kioscsys.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import kioscsys.conexion.ConexionBD;
import kioscsys.modelo.Modulo;
import kioscsys.modelo.Usuario;

/**
 * Menú principal de selección. Ofrece los módulos en la barra de menú y en
 * botones laterales; cada opción se habilita o no según el rol del usuario
 * (polimorfismo: se le pregunta a Usuario.puedeAcceder sin saber si es
 * Vendedor o Administrador).
 */
public class MenuPrincipalView extends JFrame {

    private final Usuario usuario;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<Modulo, JPanel> panelesCreados = new EnumMap<>(Modulo.class);

    public MenuPrincipalView(Usuario usuario) {
        super("KioscSys - Kiosco Don Mateo");
        this.usuario = usuario;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(1000, 640));

        setJMenuBar(crearBarraMenu());
        add(crearBotonera(), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        contenido.add(crearInicio(), "INICIO");
        tarjetas.show(contenido, "INICIO");

        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem inicio = new JMenuItem("Inicio");
        inicio.addActionListener(e -> tarjetas.show(contenido, "INICIO"));
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        cerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> salir());
        archivo.add(inicio);
        archivo.add(cerrarSesion);
        archivo.addSeparator();
        archivo.add(salir);

        JMenu modulos = new JMenu("Módulos");
        // Estructura repetitiva sobre todos los módulos del sistema
        for (Modulo m : Modulo.values()) {
            JMenuItem item = new JMenuItem(m.getTitulo());
            item.setEnabled(usuario.puedeAcceder(m));
            item.addActionListener(e -> abrir(m));
            modulos.add(item);
        }

        barra.add(archivo);
        barra.add(modulos);
        return barra;
    }

    private JPanel crearBotonera() {
        JPanel botonera = new JPanel(new GridLayout(0, 1, 0, 8));
        botonera.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        botonera.setBackground(Estilo.PRIMARIO);
        for (Modulo m : Modulo.values()) {
            JButton b = Estilo.boton(m.getTitulo());
            b.setToolTipText(m.getCasoDeUso());
            if (usuario.puedeAcceder(m)) {
                b.addActionListener(e -> abrir(m));
            } else {
                b.setEnabled(false);
                b.setToolTipText("Solo disponible para el dueño/administrador");
            }
            botonera.add(b);
        }
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBackground(Estilo.PRIMARIO);
        envoltorio.add(botonera, BorderLayout.NORTH);
        return envoltorio;
    }

    private JPanel crearInicio() {
        JPanel inicio = new JPanel(new BorderLayout());
        inicio.setBorder(Estilo.margen(32));
        JLabel saludo = Estilo.titulo("Hola, " + usuario.getNombre() + ".");
        JLabel ayuda = new JLabel("<html>Elegí un módulo en el menú de la izquierda o en <b>Módulos</b>.<br>"
                + "Tu rol: <b>" + usuario.getNombreRol() + "</b>.</html>");
        ayuda.setFont(Estilo.NORMAL);
        ayuda.setVerticalAlignment(SwingConstants.TOP);
        inicio.add(saludo, BorderLayout.NORTH);
        inicio.add(ayuda, BorderLayout.CENTER);
        return inicio;
    }

    private JPanel crearBarraEstado() {
        JPanel estado = new JPanel(new BorderLayout());
        estado.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        estado.add(new JLabel("Usuario: " + usuario), BorderLayout.WEST);
        estado.add(new JLabel("Base: " + ConexionBD.getInstancia().getUrl()), BorderLayout.EAST);
        return estado;
    }

    /** Abre un módulo. Cada pantalla se crea la primera vez que se usa. */
    private void abrir(Modulo modulo) {
        if (!usuario.puedeAcceder(modulo)) {   // doble control de permisos (RNF-05)
            Estilo.aviso(this, "No tenés permiso para acceder a " + modulo.getTitulo() + ".");
            return;
        }
        JPanel panel = panelesCreados.get(modulo);
        if (panel == null) {
            switch (modulo) {
                case VENTAS:
                    panel = new VentaPanel(usuario);
                    break;
                case PRODUCTOS:
                    panel = new ProductosPanel();
                    break;
                case VALORIZACION:
                    panel = new ValorizacionPanel();
                    break;
                default:
                    return;
            }
            panelesCreados.put(modulo, panel);
            contenido.add(panel, modulo.name());
        }
        if (panel instanceof Recargable r) {
            r.recargar();   // datos frescos cada vez que se entra
        }
        tarjetas.show(contenido, modulo.name());
    }

    private void cerrarSesion() {
        dispose();
        new LoginView().setVisible(true);
    }

    private void salir() {
        int opcion = JOptionPane.showConfirmDialog(this, "¿Cerrar KioscSys?", "Salir",
                JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            ConexionBD.getInstancia().cerrar();
            System.exit(0);
        }
    }
}

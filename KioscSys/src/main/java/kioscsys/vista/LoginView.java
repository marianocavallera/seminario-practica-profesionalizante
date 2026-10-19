package kioscsys.vista;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import kioscsys.controlador.LoginController;
import kioscsys.excepciones.AutenticacionException;
import kioscsys.modelo.Usuario;

/** Pantalla de inicio de sesión (CU-01, prototipo de la Figura 9 del TP2). */
public class LoginView extends JFrame {

    private final LoginController controller = new LoginController();
    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(16);

    public LoginView() {
        super("KioscSys - Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(Estilo.margen(24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = Estilo.titulo("KioscSys");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(titulo, c);

        JLabel subtitulo = new JLabel("Kiosco Don Mateo", SwingConstants.CENTER);
        c.gridy = 1;
        panel.add(subtitulo, c);

        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0;
        panel.add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        panel.add(txtUsuario, c);

        c.gridy = 3; c.gridx = 0;
        panel.add(new JLabel("Contraseña:"), c);
        c.gridx = 1;
        panel.add(txtPassword, c);

        JButton btnIngresar = Estilo.boton("Ingresar");
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        panel.add(btnIngresar, c);

        btnIngresar.addActionListener(e -> ingresar());
        txtPassword.addActionListener(e -> ingresar());   // Enter en la contraseña
        txtUsuario.addActionListener(e -> txtPassword.requestFocus());
        getRootPane().setDefaultButton(btnIngresar);

        add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario usuario = controller.autenticar(txtUsuario.getText(), txtPassword.getPassword());
            new MenuPrincipalView(usuario).setVisible(true);
            dispose();
        } catch (AutenticacionException e) {
            Estilo.aviso(this, e.getMessage());
            txtPassword.setText("");
            txtPassword.requestFocus();
        } catch (SQLException e) {
            Estilo.errorBD(this, e);
        }
    }
}

package kioscsys;

import java.sql.SQLException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import kioscsys.conexion.ConexionBD;
import kioscsys.vista.Estilo;
import kioscsys.vista.LoginView;

/**
 * Punto de entrada de KioscSys.
 *
 * Estructura del proyecto (arquitectura MVC + DAO definida en el TP2):
 *   modelo       -> clases del negocio (Producto, Venta, Usuario y subclases)
 *   vista        -> pantallas Swing
 *   controlador  -> coordinan cada caso de uso
 *   dao          -> único lugar con sentencias SQL
 *   conexion     -> conexión JDBC con MySQL (Singleton)
 *   excepciones  -> errores propios del negocio
 *   util         -> algoritmos de ordenación/búsqueda, formatos y hash
 */
public class KioscSys {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            usarAspectoNimbus();
            // Antes de mostrar el login se verifica que MySQL responda
            try {
                ConexionBD.getInstancia().getConexion();
            } catch (SQLException e) {
                Estilo.error(null, "No se pudo conectar a MySQL.\n\n"
                        + "1. Verificá que el servidor MySQL esté iniciado.\n"
                        + "2. Verificá que exista la base kioscsys (script sql/kioscsys.sql).\n"
                        + "3. Revisá usuario y contraseña en config.properties.\n\n"
                        + "Detalle: " + e.getMessage());
                System.exit(1);
            }
            new LoginView().setVisible(true);
        });
    }

    private static void usarAspectoNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // Si Nimbus no está disponible se usa el aspecto por defecto de Java.
        }
    }
}

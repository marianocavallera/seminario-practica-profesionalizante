package kioscsys.controlador;

import java.sql.SQLException;
import kioscsys.dao.UsuarioDAO;
import kioscsys.excepciones.AutenticacionException;
import kioscsys.modelo.Usuario;

/** Caso de uso CU-01 Iniciar sesión. */
public class LoginController {

    private final UsuarioDAO usuarioDAO;

    public LoginController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Verifica usuario y contraseña.
     * Por seguridad, el mensaje es el mismo si el usuario no existe o si la
     * contraseña es incorrecta: así no se revela qué usuarios existen.
     */
    public Usuario autenticar(String username, char[] password)
            throws AutenticacionException, SQLException {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            throw new AutenticacionException("Ingresá el usuario y la contraseña.");
        }
        Usuario usuario = usuarioDAO.buscarPorUsername(username.trim());
        if (usuario == null || !usuario.verificarPassword(new String(password))) {
            throw new AutenticacionException("Usuario o contraseña incorrectos.");
        }
        if (!usuario.isActivo()) {
            throw new AutenticacionException("El usuario está dado de baja. Consultá con el dueño.");
        }
        return usuario;
    }
}

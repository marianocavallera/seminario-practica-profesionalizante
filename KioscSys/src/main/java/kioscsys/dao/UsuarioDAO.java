package kioscsys.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kioscsys.conexion.ConexionBD;
import kioscsys.modelo.Administrador;
import kioscsys.modelo.Usuario;
import kioscsys.modelo.Vendedor;

/** Acceso a la tabla usuario. */
public class UsuarioDAO {

    /** id_rol del dueño en la tabla rol (ver los INSERT de kioscsys.sql). */
    private static final int ROL_ADMINISTRADOR = 1;
    private static final int ROL_VENDEDOR = 2;

    private static final String SQL_POR_USERNAME =
            "SELECT id_usuario, nombre, apellido, username, password_hash, id_rol, activo "
          + "FROM usuario WHERE username = ?";

    /**
     * Busca un usuario por su nombre de usuario.
     * @return el usuario, o null si no existe.
     */
    public Usuario buscarPorUsername(String username) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        // PreparedStatement evita la inyección SQL: el dato nunca se concatena a la consulta.
        try (PreparedStatement ps = con.prepareStatement(SQL_POR_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearUsuario(rs);
                }
                return null;
            }
        }
    }

    /**
     * Crea el objeto de la subclase que corresponde según el rol guardado.
     * Quien llama recibe un Usuario y no necesita saber cuál es (polimorfismo).
     */
    private Usuario crearUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String username = rs.getString("username");
        String hash = rs.getString("password_hash");
        boolean activo = rs.getBoolean("activo");
        int idRol = rs.getInt("id_rol");

        switch (idRol) {
            case ROL_ADMINISTRADOR:
                return new Administrador(id, nombre, apellido, username, hash, activo);
            case ROL_VENDEDOR:
                return new Vendedor(id, nombre, apellido, username, hash, activo);
            default:
                throw new SQLException("Rol desconocido en la base de datos: " + idRol);
        }
    }
}

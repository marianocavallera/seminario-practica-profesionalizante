package kioscsys.modelo;

/**
 * Dueño del kiosco. Por la generalización del diagrama de casos de uso,
 * puede hacer todo lo que hace el Vendedor y además gestionar productos y
 * consultar la valorización del inventario y los reportes de ventas.
 */
public class Administrador extends Usuario {

    public Administrador(int idUsuario, String nombre, String apellido,
                         String username, String passwordHash, boolean activo) {
        super(idUsuario, nombre, apellido, username, passwordHash, activo);
    }

    @Override
    public String getNombreRol() {
        return "Dueño/Administrador";
    }

    @Override
    public boolean puedeAcceder(Modulo modulo) {
        return true; // acceso a todos los módulos
    }
}

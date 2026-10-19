package kioscsys.modelo;

/**
 * Empleado del mostrador. Solo puede registrar ventas (actor Vendedor del
 * diagrama de casos de uso).
 */
public class Vendedor extends Usuario {

    public Vendedor(int idUsuario, String nombre, String apellido,
                    String username, String passwordHash, boolean activo) {
        super(idUsuario, nombre, apellido, username, passwordHash, activo);
    }

    @Override
    public String getNombreRol() {
        return "Vendedor";
    }

    @Override
    public boolean puedeAcceder(Modulo modulo) {
        return modulo == Modulo.VENTAS;
    }
}

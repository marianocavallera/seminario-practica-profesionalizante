package kioscsys.modelo;

import kioscsys.util.Seguridad;

/**
 * Persona que opera KioscSys.
 *
 * ABSTRACCIÓN: la clase es abstracta porque en el kiosco no existe un
 * "usuario genérico": siempre es un Vendedor o un Administrador. Por eso no
 * se pueden crear objetos Usuario directamente, solo de sus subclases.
 *
 * ENCAPSULAMIENTO: todos los atributos son privados. El hash de la contraseña
 * ni siquiera tiene getter: la única forma de usarlo es verificarPassword().
 *
 * HERENCIA: Vendedor y Administrador heredan los datos y el comportamiento
 * común, y solo definen lo que los diferencia.
 */
public abstract class Usuario {

    private final int idUsuario;
    private final String nombre;
    private final String apellido;
    private final String username;
    private final String passwordHash;
    private final boolean activo;

    protected Usuario(int idUsuario, String nombre, String apellido,
                      String username, String passwordHash, boolean activo) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El usuario debe tener un nombre de usuario.");
        }
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.passwordHash = passwordHash;
        this.activo = activo;
    }

    /** Nombre del rol tal como se muestra en pantalla. */
    public abstract String getNombreRol();

    /**
     * Indica si este usuario puede abrir un módulo del sistema (RNF-05).
     * POLIMORFISMO: cada subclase responde distinto al mismo mensaje.
     */
    public abstract boolean puedeAcceder(Modulo modulo);

    /** Compara la contraseña ingresada contra el hash SHA-256 guardado en la base. */
    public boolean verificarPassword(String passwordIngresada) {
        if (passwordIngresada == null) {
            return false;
        }
        return Seguridad.sha256(passwordIngresada).equalsIgnoreCase(passwordHash);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + getNombreRol() + ")";
    }
}

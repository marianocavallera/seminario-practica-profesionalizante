package kioscsys.excepciones;

/** Usuario inexistente, inactivo o contraseña incorrecta al iniciar sesión (CU-01). */
public class AutenticacionException extends Exception {

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}

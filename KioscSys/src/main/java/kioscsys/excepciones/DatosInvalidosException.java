package kioscsys.excepciones;

/**
 * Datos ingresados por el usuario que no cumplen las reglas del negocio:
 * campos vacíos, texto donde va un número, precios negativos, etc.
 * El mensaje está pensado para mostrarse tal cual en pantalla.
 */
public class DatosInvalidosException extends Exception {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

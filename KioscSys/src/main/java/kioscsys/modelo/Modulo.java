package kioscsys.modelo;

/**
 * Módulos del sistema a los que se puede acceder desde el menú principal.
 * Cada tipo de usuario decide a cuáles tiene permiso (ver Usuario.puedeAcceder).
 */
public enum Modulo {
    VENTAS("Registrar venta", "CU-03"),
    PRODUCTOS("Gestionar productos", "CU-05"),
    VALORIZACION("Valorización y alertas de stock", "CU-09 / CU-11"),
    REPORTES("Reporte de ventas del día", "CU-10");

    private final String titulo;
    private final String casoDeUso;

    Modulo(String titulo, String casoDeUso) {
        this.titulo = titulo;
        this.casoDeUso = casoDeUso;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCasoDeUso() {
        return casoDeUso;
    }
}

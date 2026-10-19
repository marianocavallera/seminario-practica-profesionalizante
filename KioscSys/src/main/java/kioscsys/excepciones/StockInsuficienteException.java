package kioscsys.excepciones;

/**
 * Se lanza cuando se intenta vender más unidades de las que hay en stock
 * (regla de negocio RN-05).
 *
 * Es una excepción verificada (extends Exception) a propósito: el compilador
 * obliga a quien vende a tratar este caso, porque no es un error de
 * programación sino una situación normal del mostrador.
 */
public class StockInsuficienteException extends Exception {

    private final String producto;
    private final int disponible;
    private final int solicitado;

    public StockInsuficienteException(String producto, int disponible, int solicitado) {
        super("Stock insuficiente de \"" + producto + "\": hay " + disponible
                + " unidad(es) y se pidieron " + solicitado + ".");
        this.producto = producto;
        this.disponible = disponible;
        this.solicitado = solicitado;
    }

    public String getProducto() {
        return producto;
    }

    public int getDisponible() {
        return disponible;
    }

    public int getSolicitado() {
        return solicitado;
    }
}

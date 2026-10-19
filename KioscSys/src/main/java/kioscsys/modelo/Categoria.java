package kioscsys.modelo;

/** Rubro al que pertenece un producto (Golosinas, Bebidas, etc.). */
public class Categoria {

    private final int idCategoria;
    private final String nombre;

    public Categoria(int idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    /** Se usa para mostrar la categoría en los combos de las pantallas. */
    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Categoria c && c.idCategoria == this.idCategoria;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCategoria);
    }
}

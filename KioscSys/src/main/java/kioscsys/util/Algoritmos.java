package kioscsys.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Algoritmos de ordenación y búsqueda usados por las pantallas.
 *
 * Se implementan a mano (en lugar de usar Collections.sort) porque el
 * catálogo del kiosco ronda los 600 productos y se ordena y filtra en
 * memoria, del lado de la aplicación, sin volver a consultar la base cada
 * vez que el usuario cambia el criterio o escribe en el buscador.
 */
public final class Algoritmos {

    private Algoritmos() {
    }

    /**
     * Ordenación por inserción. Recorre la lista y va "insertando" cada
     * elemento en su lugar dentro de la parte ya ordenada.
     *
     * Se eligió porque es estable (dos productos con el mismo valor conservan
     * su orden) y es eficiente cuando la lista ya está casi ordenada, que es
     * el caso habitual: la base devuelve los productos ordenados por nombre.
     * Complejidad: O(n^2) en el peor caso, O(n) en el mejor.
     *
     * @return una lista nueva ordenada; la original no se modifica.
     */
    public static <T> List<T> ordenarPorInsercion(List<T> lista, Comparator<T> criterio) {
        List<T> resultado = new ArrayList<>(lista);
        for (int i = 1; i < resultado.size(); i++) {
            T actual = resultado.get(i);
            int j = i - 1;
            // Desplaza a la derecha los elementos mayores que "actual"
            while (j >= 0 && criterio.compare(resultado.get(j), actual) > 0) {
                resultado.set(j + 1, resultado.get(j));
                j--;
            }
            resultado.set(j + 1, actual);
        }
        return resultado;
    }

    /**
     * Búsqueda secuencial: devuelve los elementos cuyo texto contiene lo
     * buscado, sin distinguir mayúsculas ni tildes ("cafe" encuentra "Café").
     * Recorre la lista completa porque el texto puede aparecer en cualquier
     * parte del nombre, por lo que no sirve una búsqueda binaria.
     */
    public static <T> List<T> buscarSecuencial(List<T> lista, String texto, Function<T, String> campo) {
        List<T> encontrados = new ArrayList<>();
        String buscado = normalizar(texto);
        if (buscado.isEmpty()) {
            encontrados.addAll(lista);
            return encontrados;
        }
        for (T elemento : lista) {
            if (normalizar(campo.apply(elemento)).contains(buscado)) {
                encontrados.add(elemento);
            }
        }
        return encontrados;
    }

    /** Pasa a minúsculas y quita tildes para comparar textos. */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase();
    }
}

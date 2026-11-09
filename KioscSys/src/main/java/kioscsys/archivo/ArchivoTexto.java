package kioscsys.archivo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Clase abstracta base de todos los archivos de texto que genera KioscSys:
 * el ticket de cada venta, la exportación de la valorización a CSV y el
 * reporte de ventas del día.
 *
 * Aplica el patrón Método Plantilla (Template Method): el método generar()
 * define una sola vez los pasos comunes (crear la carpeta, abrir el archivo
 * con un buffer, escribir, verificar errores y cerrarlo) y cada subclase
 * completa solo lo que la diferencia: el nombre del archivo y su contenido.
 * Así la lógica de manejo de archivos y de excepciones no se repite.
 *
 * Es parte de la capa de persistencia, igual que los DAO: los controladores
 * la usan y las pantallas nunca escriben archivos directamente.
 */
public abstract class ArchivoTexto {

    private final File carpeta;

    protected ArchivoTexto(File carpeta) {
        this.carpeta = carpeta;
    }

    /** Nombre del archivo a generar, por ejemplo "ticket-000012.txt". */
    protected abstract String nombreArchivo();

    /** Escribe el contenido propio de cada tipo de archivo. */
    protected abstract void escribirContenido(PrintWriter salida);

    /**
     * Genera el archivo. Es final para que ninguna subclase cambie la
     * secuencia de pasos, solo los pasos abstractos.
     *
     * @return el archivo creado, para poder informar dónde quedó.
     * @throws IOException si no se puede crear la carpeta o escribir el archivo
     *         (disco lleno, carpeta sin permisos, archivo abierto en otro programa).
     */
    public final File generar() throws IOException {
        // Clase File: representa la ruta; no lee ni escribe datos.
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + carpeta.getAbsolutePath());
        }
        File destino = new File(carpeta, nombreArchivo());

        // Flujo de salida con buffer: junta el texto en memoria y lo escribe
        // al disco en bloques. try-with-resources lo cierra aunque haya error.
        try (PrintWriter salida = new PrintWriter(
                new BufferedWriter(new FileWriter(destino, StandardCharsets.UTF_8)))) {
            escribirContenido(salida);
            // PrintWriter no lanza excepciones al escribir: hay que consultarlo.
            if (salida.checkError()) {
                throw new IOException("Se produjo un error al escribir " + destino.getName());
            }
        }
        return destino;
    }

    /**
     * Lee un archivo de texto completo con un flujo de entrada con buffer,
     * línea por línea. Se usa para mostrar en pantalla el ticket recién emitido.
     */
    public static String leer(File archivo) throws IOException {
        StringBuilder contenido = new StringBuilder();
        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                contenido.append(linea).append(System.lineSeparator());
            }
        }
        return contenido.toString();
    }
}

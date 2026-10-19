package kioscsys.conexion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión JDBC con la base de datos MySQL "kioscsys".
 *
 * Aplica el patrón Singleton (diseñado en el TP2): existe una única instancia
 * de esta clase y se reutiliza la misma conexión durante toda la ejecución,
 * porque el sistema corre en un solo puesto (la PC del mostrador).
 *
 * Los datos de conexión se leen de config.properties, de modo que cambiar
 * de servidor (por ejemplo, una segunda sucursal, RNF-07) no requiere
 * modificar el código.
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIG = "config.properties";
    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String password;
    private Connection conexion;

    /** Constructor privado: nadie fuera de la clase puede crear otra instancia. */
    private ConexionBD() {
        Properties config = cargarConfiguracion();
        this.url = config.getProperty("db.url", "jdbc:mysql://localhost:3306/kioscsys");
        this.usuario = config.getProperty("db.usuario", "root");
        this.password = config.getProperty("db.password", "");
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /**
     * Devuelve la conexión abierta, o la vuelve a abrir si se cerró
     * (por ejemplo, si MySQL se reinició mientras la aplicación estaba abierta).
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            conexion = DriverManager.getConnection(url, usuario, password);
        }
        return conexion;
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            // Al cerrar la aplicación un error aquí no afecta los datos.
            System.err.println("No se pudo cerrar la conexión: " + e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    /**
     * Busca config.properties primero junto al programa (carpeta del proyecto
     * o del .jar) y, si no existe, dentro de los recursos de la aplicación.
     */
    private Properties cargarConfiguracion() {
        Properties props = new Properties();
        Path archivoExterno = Path.of(ARCHIVO_CONFIG);
        try {
            if (Files.exists(archivoExterno)) {
                try (InputStream in = new FileInputStream(archivoExterno.toFile())) {
                    props.load(in);
                }
            } else {
                try (InputStream in = ConexionBD.class.getResourceAsStream("/" + ARCHIVO_CONFIG)) {
                    if (in != null) {
                        props.load(in);
                    }
                }
            }
        } catch (IOException e) {
            // Si el archivo no se puede leer se usan los valores por defecto.
            System.err.println("No se pudo leer " + ARCHIVO_CONFIG + ": " + e.getMessage());
        }
        return props;
    }
}

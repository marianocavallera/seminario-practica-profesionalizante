package kioscsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cálculo del hash SHA-256 de las contraseñas (RNF-05).
 * Produce el mismo resultado que SHA2('texto', 256) de MySQL, que es
 * como se guardaron los usuarios de prueba en el script kioscsys.sql.
 */
public final class Seguridad {

    private Seguridad() {
        // clase de utilidades: no se instancia
    }

    public static String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 está garantizado en toda JVM; si faltara, es un error grave.
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}

package kioscsys.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos de pantalla con la convención argentina: $ 1.500,00 y dd/MM/yyyy. */
public final class Formato {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() {
    }

    public static String moneda(BigDecimal importe) {
        NumberFormat nf = NumberFormat.getNumberInstance(ARGENTINA);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "$ " + nf.format(importe == null ? BigDecimal.ZERO : importe);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha.format(FECHA_HORA);
    }
}

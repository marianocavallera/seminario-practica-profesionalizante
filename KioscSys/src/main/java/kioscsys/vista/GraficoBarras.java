package kioscsys.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import kioscsys.util.Formato;

/**
 * Gráfico de barras simple dibujado con Swing (sin librerías externas).
 * Recibe un ARREGLO de importes, uno por hora, y dibuja solo el tramo
 * [desde, hasta]: el horario de atención del kiosco.
 */
public class GraficoBarras extends JPanel {

    private BigDecimal[] valores = new BigDecimal[0];
    private int desde;
    private int hasta;
    private int resaltada = -1;

    public GraficoBarras() {
        setPreferredSize(new Dimension(520, 300));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Importe vendido por hora"));
    }

    /** Recibe los datos a dibujar y la barra a resaltar (hora pico). */
    public void setDatos(BigDecimal[] valores, int desde, int hasta, int resaltada) {
        this.valores = valores;
        this.desde = desde;
        this.hasta = hasta;
        this.resaltada = resaltada;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (valores.length == 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int izq = 20, der = 20, arriba = 40, abajo = 34;
        int ancho = getWidth() - izq - der;
        int alto = getHeight() - arriba - abajo;
        int barras = hasta - desde + 1;
        int paso = Math.max(1, ancho / barras);

        // Máximo del tramo: recorrido del arreglo
        BigDecimal maximo = BigDecimal.ZERO;
        for (int h = desde; h <= hasta; h++) {
            if (valores[h].compareTo(maximo) > 0) {
                maximo = valores[h];
            }
        }

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int base = arriba + alto;
        g2.setColor(new Color(0xCCCCCC));
        g2.drawLine(izq, base, izq + paso * barras, base);

        for (int h = desde; h <= hasta; h++) {
            int x = izq + (h - desde) * paso;
            int largo = maximo.signum() == 0 ? 0
                    : valores[h].multiply(BigDecimal.valueOf(alto)).divide(maximo, 0, java.math.RoundingMode.HALF_UP).intValue();
            g2.setColor(h == resaltada ? new Color(0xC55A11) : Estilo.PRIMARIO);
            g2.fillRect(x + 3, base - largo, paso - 6, largo);

            g2.setColor(Color.DARK_GRAY);
            String hora = String.valueOf(h);
            g2.drawString(hora, x + (paso - fm.stringWidth(hora)) / 2, base + 14);
            if (h == resaltada) {
                String texto = Formato.moneda(valores[h]);
                int tx = Math.max(izq, Math.min(x + (paso - fm.stringWidth(texto)) / 2, getWidth() - der - fm.stringWidth(texto)));
                g2.drawString(texto, tx, base - largo - 4);
            }
        }
        g2.setColor(Color.GRAY);
        g2.drawString("Hora del día (en naranja, la hora pico)", izq, base + 28);
    }
}

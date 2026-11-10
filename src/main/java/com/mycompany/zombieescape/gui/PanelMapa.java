package com.mycompany.zombieescape.gui;

import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Posicion;
import com.mycompany.zombieescape.modelo.TipoCelda;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Dibuja el mapa celda por celda y, encima, las figuras (jugadores y zombies).
 *
 * @author dylnr
 */
public class PanelMapa extends JPanel {
    public static final int TAM = 32;                 // pixeles por celda
    private static final String CARPETA_IMG = "Img";

    private Mapa mapa;
    private ArrayList<Figura> figuras = new ArrayList<>();

    // Vista limitada del sniper: si centroVision es null se ve todo el mapa
    private Posicion centroVision;
    private int radioVision;
    private Posicion apuntada;        // celda bajo el mouse del sniper (para dibujar la mira)

    private static final Color NIEBLA = Color.BLACK;   // fuera de la mira no se ve nada
    private static final Color ROJO_MIRA = new Color(230, 60, 60);

    // Aviso escrito encima del mapa (esperando, nivel superado, espectador...). null = ninguno
    private String avisoTitulo;
    private String avisoDetalle;
    private Color avisoColor;
    private boolean avisoCentrado;    // true: en medio (fin de nivel); false: franja arriba (para no tapar el juego)

    // Imágenes ya cargadas, para no leer el archivo cada vez que se repinta
    private HashMap<String, Image> imagenes = new HashMap<>();

    public PanelMapa() {
        setBackground(Color.BLACK);
    }

    public void setMapa(Mapa mapa) {
        this.mapa = mapa;
        setPreferredSize(new Dimension(mapa.getColumnas() * TAM, mapa.getFilas() * TAM));
        revalidate();   // avisa a la ventana que el tamaño cambió
        repaint();
    }

    public void setFiguras(ArrayList<Figura> figuras) {
        this.figuras = figuras;
        repaint();
    }

    // El sniper solo ve un cuadro alrededor de centro; null = ver todo
    public void setVision(Posicion centro, int radio) {
        this.centroVision = centro;
        this.radioVision = radio;
        repaint();
    }

    public void setAviso(String titulo, String detalle, Color color, boolean centrado) {
        this.avisoTitulo = titulo;
        this.avisoDetalle = detalle;
        this.avisoColor = color;
        this.avisoCentrado = centrado;
        repaint();
    }

    public void quitarAviso() {
        setAviso(null, null, null, false);
    }

    public void setApuntada(Posicion celda) {
        this.apuntada = celda;
        repaint();
    }

    // Convierte un punto del mouse (en pixeles) a la celda del mapa. null si cae fuera.
    // Es la cuenta del dibujo al revés: allá x = columna * TAM, aquí columna = x / TAM.
    public Posicion celdaEn(int x, int y) {
        if (mapa == null || x < 0 || y < 0) return null;
        Posicion p = new Posicion(y / TAM, x / TAM);
        return mapa.estaDentro(p.getFila(), p.getColumna()) ? p : null;
    }

    private boolean dentroDeVision(int fila, int columna) {
        if (centroVision == null) return true;
        return Math.abs(fila - centroVision.getFila()) <= radioVision
            && Math.abs(columna - centroVision.getColumna()) <= radioVision;
    }

    // Devuelve la imagen Img/<nombre>.png, cargándola solo la primera vez
    private Image getImagen(String nombre) {
        if (nombre == null) return null;          // por ejemplo, una tarjeta de respawn vencida
        if (!imagenes.containsKey(nombre)) {
            File archivo = new File(CARPETA_IMG, nombre + ".png");
            Image img = null;
            if (archivo.exists()) {
                img = new ImageIcon(archivo.getPath()).getImage();
            } else {
                System.out.println("No se encontró la imagen: " + archivo.getPath());
            }
            imagenes.put(nombre, img);
        }
        return imagenes.get(nombre);
    }

    // Qué imagen corresponde a cada símbolo del mapa
    private String nombreImagenCelda(char tipo) {
        switch (tipo) {
            case TipoCelda.PARED:     return "pared";
            case TipoCelda.ESCONDITE: return "escondite";
            case TipoCelda.OBSTACULO: return "obstaculo";
            case TipoCelda.ENTRADA:   return "entrada";
            case TipoCelda.SALIDA:    return "salida";
            default:                  return "camino";
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapa == null) {                 // todavía no hay partida: solo el aviso de espera
            dibujarAviso((Graphics2D) g);
            return;
        }

        // 1. El mapa
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int c = 0; c < mapa.getColumnas(); c++) {
                Image img = getImagen(nombreImagenCelda(mapa.getCelda(f, c)));
                int x = c * TAM;
                int y = f * TAM;
                if (img != null) {
                    g.drawImage(img, x, y, TAM, TAM, this);
                } else {
                    g.setColor(Color.MAGENTA);   // color llamativo = falta una imagen
                    g.fillRect(x, y, TAM, TAM);
                }
            }
        }

        // 2. Jugadores y zombies encima
        Graphics2D g2 = (Graphics2D) g;
        for (Figura fig : figuras) {
            // Lo que el sniper no alcanza a ver no se dibuja (si no, vería zombies "en la niebla")
            if (!dentroDeVision(fig.getFila(), fig.getColumna())) continue;
            int x = fig.getColumna() * TAM;
            int y = fig.getFila() * TAM;

            if (fig.isOculta()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            }
            Image img = getImagen(fig.getImagen());
            if (img != null) {
                g2.drawImage(img, x, y, TAM, TAM, this);
            }
            g2.setComposite(AlphaComposite.SrcOver);   // volver a opaco

            if (fig.getEtiqueta() != null) {
                dibujarEtiqueta(g2, fig.getEtiqueta(), x, y);
            }
        }

        // 3. Niebla del sniper y la mira
        if (centroVision != null) {
            dibujarNiebla(g2);
        }

        // 4. Aviso encima de todo
        dibujarAviso(g2);
    }

    // Una franja oscura con un título grande y una línea de detalle
    private void dibujarAviso(Graphics2D g2) {
        if (avisoTitulo == null) return;
        int alto = avisoDetalle == null ? 56 : 80;
        int y = avisoCentrado ? (getHeight() - alto) / 2 : 0;

        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, y, getWidth(), alto);

        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(avisoColor);
        g2.drawString(avisoTitulo, (getWidth() - fm.stringWidth(avisoTitulo)) / 2, y + 36);

        if (avisoDetalle != null) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(228, 228, 231));
            g2.drawString(avisoDetalle, (getWidth() - fm.stringWidth(avisoDetalle)) / 2, y + 62);
        }
    }

    private void dibujarNiebla(Graphics2D g2) {
        g2.setColor(NIEBLA);
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int c = 0; c < mapa.getColumnas(); c++) {
                if (!dentroDeVision(f, c)) {
                    g2.fillRect(c * TAM, f * TAM, TAM, TAM);
                }
            }
        }
        // Borde del área visible
        int lado = (radioVision * 2 + 1) * TAM;
        int x = (centroVision.getColumna() - radioVision) * TAM;
        int y = (centroVision.getFila() - radioVision) * TAM;
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(120, 200, 80));
        g2.drawRect(x, y, lado, lado);

        // Mira sobre la celda apuntada (solo si está dentro de lo que ve)
        if (apuntada != null && dentroDeVision(apuntada.getFila(), apuntada.getColumna())) {
            int ax = apuntada.getColumna() * TAM;
            int ay = apuntada.getFila() * TAM;
            g2.setColor(ROJO_MIRA);
            g2.drawRect(ax + 1, ay + 1, TAM - 2, TAM - 2);
            g2.drawLine(ax + TAM / 2, ay + 4, ax + TAM / 2, ay + TAM - 4);
            g2.drawLine(ax + 4, ay + TAM / 2, ax + TAM - 4, ay + TAM / 2);
        }
        g2.setStroke(new BasicStroke(1));
    }

    // Nombre del jugador centrado debajo de su celda, con sombra para que se lea
    private void dibujarEtiqueta(Graphics2D g2, String texto, int x, int y) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (TAM - fm.stringWidth(texto)) / 2;
        int ty = y + TAM + fm.getAscent() - 2;
        g2.setColor(Color.BLACK);
        g2.drawString(texto, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(texto, tx, ty);
    }
}

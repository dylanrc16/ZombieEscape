package com.mycompany.zombieescape.gui;

import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.TipoCelda;
import java.awt.AlphaComposite;
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

    // Devuelve la imagen Img/<nombre>.png, cargándola solo la primera vez
    private Image getImagen(String nombre) {
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
        if (mapa == null) return;

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

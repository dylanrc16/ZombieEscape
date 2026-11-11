package com.mycompany.zombieescape.gui;

import com.mycompany.zombieescape.Rutas;
import com.mycompany.zombieescape.logica.CargadorMapas;
import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Posicion;
import com.mycompany.zombieescape.modelo.TipoCelda;
import com.mycompany.zombieescape.modelo.Zombie;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Portada del login: un pedazo de un mapa real del juego, oscurecido, con zombies
 * caminando de verdad (usan la misma clase Zombie y su método mover), y encima el logo.
 *
 * @author dylnr
 */
public class PanelPortada extends JPanel {
    private static final int TAM = PanelMapa.TAM;                 // 32 px por celda, igual que el juego
    private static final String ESCENARIO = "nivel06.txt";
    private static final int CANTIDAD_ZOMBIES = 7;
    private static final int PASO_MS = 450;                       // cada cuánto dan un paso

    private Mapa mapa;
    private ArrayList<Zombie> zombies = new ArrayList<>();
    private HashMap<String, Image> imagenes = new HashMap<>();
    private Image logo = Recursos.imagen("logo_dd");

    // javax.swing.Timer y no Thread.sleep: el Timer avisa en el hilo de Swing,
    // así que mover y repintar no choca con el dibujo
    private Timer animacion = new Timer(PASO_MS, e -> darPaso());

    public PanelPortada() {
        setBackground(new Color(12, 12, 14));
        setPreferredSize(new Dimension(380, 400));   // el alto lo pone el formulario de al lado
        try {
            mapa = CargadorMapas.cargar(Rutas.archivo(CargadorMapas.CARPETA, ESCENARIO).getPath());
            crearZombies();
        } catch (IOException ex) {
            mapa = null;   // sin escenario: la portada se ve solo con el logo
        }
    }

    // Zombies en calles que caen dentro de lo que se ve de la portada
    private void crearZombies() {
        ArrayList<Posicion> calles = new ArrayList<>();
        for (Posicion p : mapa.buscarCeldas(TipoCelda.CAMINO)) {
            if (p.getColumna() < 12 && p.getFila() > 4) calles.add(p);
        }
        Collections.shuffle(calles);
        Direccion[] direcciones = Direccion.values();
        for (int i = 0; i < CANTIDAD_ZOMBIES && i < calles.size(); i++) {
            zombies.add(new Zombie(calles.get(i), direcciones[i % direcciones.length], PASO_MS));
        }
    }

    private void darPaso() {
        for (Zombie z : zombies) {
            z.mover(mapa);
        }
        repaint();
    }

    // La animación corre solo mientras el panel está en pantalla
    @Override
    public void addNotify() {
        super.addNotify();
        if (mapa != null) animacion.start();
    }

    @Override
    public void removeNotify() {
        animacion.stop();
        super.removeNotify();
    }

    private Image imagen(String nombre) {
        if (!imagenes.containsKey(nombre)) {
            imagenes.put(nombre, Recursos.imagen(nombre));
        }
        return imagenes.get(nombre);
    }

    private String nombreCelda(char tipo) {
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
        Graphics2D g2 = (Graphics2D) g;
        int ancho = getWidth();
        int alto = getHeight();

        // 1. El mapa (si el panel es más alto que el mapa, las filas se repiten)
        if (mapa != null) {
            for (int y = 0; y < alto; y += TAM) {
                for (int x = 0; x < ancho; x += TAM) {
                    int f = (y / TAM) % mapa.getFilas();
                    int c = (x / TAM) % mapa.getColumnas();
                    Image img = imagen(nombreCelda(mapa.getCelda(f, c)));
                    if (img != null) g2.drawImage(img, x, y, TAM, TAM, this);
                }
            }
            for (Zombie z : zombies) {
                Image img = imagen(z.getImagen());
                if (img != null) {
                    g2.drawImage(img, z.getPosicion().getColumna() * TAM, z.getPosicion().getFila() * TAM, TAM, TAM, this);
                }
            }
        }

        // 2. Oscurecer: más oscuro arriba (donde va el logo) y abajo
        g2.setPaint(new GradientPaint(0, 0, new Color(12, 12, 14, 250), 0, alto * 0.55f, new Color(12, 12, 14, 150)));
        g2.fillRect(0, 0, ancho, (int) (alto * 0.55f));
        g2.setPaint(new GradientPaint(0, alto * 0.55f, new Color(12, 12, 14, 150), 0, alto, new Color(12, 12, 14, 220)));
        g2.fillRect(0, (int) (alto * 0.55f), ancho, alto - (int) (alto * 0.55f));

        // 3. Logo, título y lema
        int y = 46;
        if (logo != null) {
            g2.drawImage(logo, (ancho - logo.getWidth(this)) / 2, y, this);
            y += logo.getHeight(this) + 34;
        }
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        texto(g2, "ZOMBI ESCAPE", new Font("SansSerif", Font.BOLD, 34), new Color(120, 200, 80), y);
        texto(g2, "Escapen juntos o mueran todos", new Font("SansSerif", Font.ITALIC, 14), new Color(200, 200, 205), y + 28);
        texto(g2, "Proyecto II  ·  Programación Orientada a Objetos", new Font("SansSerif", Font.PLAIN, 11),
                new Color(150, 150, 160), alto - 18);
    }

    // Texto centrado con sombra, para que se lea sobre el mapa
    private void texto(Graphics2D g2, String texto, Font fuente, Color color, int y) {
        g2.setFont(fuente);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(texto)) / 2;
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(texto, x + 2, y + 2);
        g2.setColor(color);
        g2.drawString(texto, x, y);
    }
}

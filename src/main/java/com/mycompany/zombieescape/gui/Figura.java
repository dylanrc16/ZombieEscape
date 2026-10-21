package com.mycompany.zombieescape.gui;

/**
 * Algo que se dibuja encima del mapa: un jugador, un zombie, etc.
 * La GUI no necesita saber qué clase del modelo es; solo qué imagen
 * usar, en qué celda y qué texto poner debajo.
 *
 * @author dylnr
 */
public class Figura {
    private String imagen;     // nombre del archivo en Img, sin ".png"
    private int fila;
    private int columna;
    private String etiqueta;   // nombre del jugador (null si no lleva)
    private boolean oculta;    // true = se dibuja semitransparente (escondido)

    public Figura(String imagen, int fila, int columna, String etiqueta, boolean oculta) {
        this.imagen = imagen;
        this.fila = fila;
        this.columna = columna;
        this.etiqueta = etiqueta;
        this.oculta = oculta;
    }

    public String getImagen() { return imagen; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    public String getEtiqueta() { return etiqueta; }
    public boolean isOculta() { return oculta; }
}

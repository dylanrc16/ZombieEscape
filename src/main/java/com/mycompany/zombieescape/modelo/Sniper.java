package com.mycompany.zombieescape.modelo;

public class Sniper extends Jugador {
    public static final int BALAS_POR_ESCENARIO = 10;
    public static final int RADIOVISION = 1;         // la mira es un cuadro de 3x3 (público: la ventana dibuja el mismo)

    private int countBalas = BALAS_POR_ESCENARIO;

    // La posición (heredada de Jugador) es el CENTRO de su mira.
    // El sniper no camina por el mapa; lo que se mueve es la mira (con clic derecho).
    public Sniper(String nombre) {
        super(nombre);
    }

    public int getBalas() {
        return countBalas; 
    }
    public boolean tieneBalas() {
        return countBalas > 0; 
    }

    public void gastarBala() {
        if (countBalas > 0) countBalas--;
    }

    public void recargar() {
        countBalas = BALAS_POR_ESCENARIO; 
    }

    // Lleva la mira directo a esa celda (puede ser sobre una pared: apunta desde arriba),
    // siempre que esté dentro del mapa.
    public void apuntarA(Posicion destino, Mapa mapa) {
        if (mapa.estaDentro(destino.getFila(), destino.getColumna())) {
            posicion = destino;
        }
        ultimoMovimiento = System.currentTimeMillis();
    }

    // ¿Esa celda está dentro de su cuadro de visión?
    public boolean puedeVer(Posicion p) {
        return Math.abs(p.getFila() - posicion.getFila()) <= RADIOVISION
            && Math.abs(p.getColumna() - posicion.getColumna()) <= RADIOVISION;
    }

    @Override
    public String getImagen() {
        return "sniper"; 
    }

    @Override
    public boolean puedeMoverse() {
        return false; 
    }
}

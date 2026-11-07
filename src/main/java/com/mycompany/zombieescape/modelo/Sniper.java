package com.mycompany.zombieescape.modelo;

public class Sniper extends Jugador {
    public static final int BALAS_POR_ESCENARIO = 10;
    private static final int RADIOVISION = 3;        // ve un cuadro de 7x7

    private int countBalas = BALAS_POR_ESCENARIO;

    // La posición (heredada de Jugador) es el CENTRO del área que ve
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

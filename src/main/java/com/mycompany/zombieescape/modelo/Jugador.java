package com.mycompany.zombieescape.modelo;

import java.io.Serializable;

// Lo que comparten todos los humanos: Superviviente y Sniper
public abstract class Jugador implements Serializable {
    public static final int ESPERA_MOVIMIENTO = 150;    // ms entre pasos (el cliente manda a este ritmo)
    private static final int TOLERANCIA = 40;           // margen por el retraso de la red

    protected String nombre;
    protected Posicion posicion;
    protected int puntos;
    protected boolean vivo = true;
    protected long ultimoMovimiento;        // cuándo dio su último paso (o movió la mira)

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre; 
    }
    public Posicion getPosicion() {
        return posicion; 
    }
    public int getPuntos() {
        return puntos; 
    }
    public boolean isVivo() {
        return vivo; 
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion; 
    }
    public void sumarPuntos(int cantidad) {
        puntos += cantidad; 
    }
    public void morir() {
        vivo = false; 
    }
    public void revivir() {
        vivo = true; 
    }

    // Evita que se mueva más rápido de lo permitido (por ejemplo, con un cliente modificado)
    public boolean listoParaMover() {
        return System.currentTimeMillis() - ultimoMovimiento >= ESPERA_MOVIMIENTO - TOLERANCIA;
    }

    // Cada hijo dice qué imagen lo representa (polimorfismo)
    public abstract String getImagen();

    // Cada hijo dice si camina por el mapa (el sniper no: solo mueve su mira)
    public abstract boolean puedeMoverse();
}

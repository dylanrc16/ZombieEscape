package com.mycompany.zombieescape.modelo;

import java.io.Serializable;

// Lo que comparten todos los humanos: Superviviente y Sniper
public abstract class Jugador implements Serializable {
    protected String nombre;
    protected Posicion posicion;
    protected int puntos;
    protected boolean vivo = true;

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public Posicion getPosicion() { return posicion; }
    public int getPuntos() { return puntos; }
    public boolean isVivo() { return vivo; }

    public void setPosicion(Posicion posicion) { this.posicion = posicion; }
    public void sumarPuntos(int cantidad) { puntos += cantidad; }
    public void morir() { vivo = false; }

    // Cada hijo dice qué imagen lo representa (polimorfismo)
    public abstract String getImagen();

    // Cada hijo dice si puede caminar por el mapa
    public abstract boolean puedeMoverse();
}

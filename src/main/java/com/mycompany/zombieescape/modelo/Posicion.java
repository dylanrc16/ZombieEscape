/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.modelo;

/**
 *
 * @author dylnr
 */
public class Posicion {
    private final int fila;
    private final int columna;

    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    //posicion a la que entra al dar un paso en esa direccion
    public Posicion siguiente(Direccion d) {
        return new Posicion(fila + d.getDy(), columna + d.getDx());
    }

    // Dos posiciones son iguales si tienen la misma fila y columna
    public boolean esIgual(Posicion otra) {
        return otra != null && fila == otra.fila && columna == otra.columna;
    }

    @Override
    public String toString() {
        return "(" + fila + ", " + columna + ")";
    }
}

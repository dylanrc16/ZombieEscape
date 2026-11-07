/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.modelo;

/**
 *
 * @author dylnr
 */



// Hacia dónde mira o se mueve un personaje
public enum Direccion {
    ARRIBA(0, -1),
    ABAJO(0, 1),
    IZQUIERDA(-1, 0),
    DERECHA(1, 0);

    private final int dx; // cambio en columna
    private final int dy; // cambio en fila

    Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx; 
    }
    public int getDy() {
        return dy; 
    }

    public Direccion girarDerecha() {
        switch (this) {
            case ARRIBA:    return DERECHA;
            case DERECHA:   return ABAJO;
            case ABAJO:     return IZQUIERDA;
            default:        return ARRIBA;      // IZQUIERDA
        }
    }

    public Direccion girarIzquierda() {
        switch (this) {
            case ARRIBA:    return IZQUIERDA;
            case IZQUIERDA: return ABAJO;
            case ABAJO:     return DERECHA;
            default:        return ARRIBA;      // DERECHA
        }
    }

    public Direccion opuesta() {
        return girarDerecha().girarDerecha();
    }

    // Convierte el texto de un mensaje ("arriba", "ARRIBA") en una dirección
    public static Direccion textoAMovimiento(String movimiento) {
        if (movimiento == null) return null;
        try {
            return valueOf(movimiento.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;                        // texto que no es una dirección
        }
    }

    // "arriba", "abajo"... para armar el nombre de la imagen: "zombie_" + getNombre()
    public String getNombre() {
        return name().toLowerCase();
    }
}
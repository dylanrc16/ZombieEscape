/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.modelo;

/**
 *
 * @author dylnr
 */

//Clase para donde tiene que ir cada personaje
public class Direccion {
    public static final Direccion ARRIBA    = new Direccion(0, -1);
    public static final Direccion ABAJO     = new Direccion(0, 1);
    public static final Direccion IZQUIERDA = new Direccion(-1, 0);
    public static final Direccion DERECHA   = new Direccion(1, 0);
    
    private int dx; // cambio en columna
    private int dy; // cambio en fila

    private Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    public static Direccion textoAMovimiento(String movimiento){
        movimiento.toUpperCase();
        switch(movimiento){
            case "ARRIBA" -> {
                return ARRIBA;
            }
            case "ABAJO" -> {
                return ABAJO;
            }
            case "DERECHA" -> {
                return DERECHA;
            }
            case "IZQUIERDA" -> {
                return IZQUIERDA;
            }
}
        return null;
              
                    
    }  
                   
    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
    
}

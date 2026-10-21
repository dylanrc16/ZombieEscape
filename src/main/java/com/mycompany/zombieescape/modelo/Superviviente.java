/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.modelo;
/**
 *
 * @author dylnr
 */
public class Superviviente extends Jugador implements IMovible {
   
    private int daño;
    private boolean escondido;
    Direccion direccion;

    public Superviviente(String nombre) {
        super(nombre);
    }
    
    

    @Override
    public void mover() {
         
    }
    
    
}

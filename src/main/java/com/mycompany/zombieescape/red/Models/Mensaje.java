/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Models;

import java.io.Serializable;

/**
  NOMBRE        0: "nombre"  1: rol  
  
 */

public class Mensaje implements Serializable {
    public String tipo;
    public String[] parametros;

    public Mensaje(String tipo, int cantidadParametros) {
        this.tipo = tipo;
        parametros =  new String[cantidadParametros];        
    }

    @Override
    public String toString() {
        String str = tipo;
        for (int i = 0; i < parametros.length; i++) {
            str += "  " + parametros[i];
        }
        return str;
    }
    
    
    
    
    
    
}

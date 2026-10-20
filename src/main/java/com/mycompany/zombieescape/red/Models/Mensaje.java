/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Models;

import java.io.Serializable;

/**
  MENSAJE       0: Nombre emisor  1: hola mundo
  PRIVADO       0: hola       1: Dylan
  DISPARO       0: x          1: x       2: tipo_munición
  NOMBRE        0: Diego
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

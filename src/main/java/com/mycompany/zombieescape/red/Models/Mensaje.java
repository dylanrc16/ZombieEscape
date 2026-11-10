/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Models;

import java.io.Serializable;

/**
  Cliente -> Servidor
  NOMBRE        0: nombre   1: rol ("SUPERVIVIENTE" o "SNIPER")
  MENSAJE       0: emisor   1: texto          (el servidor pone el emisor real)
  PRIVADO       0: destinatario   1: texto
  MOVE          0: dirección ("ARRIBA", "ABAJO", "IZQUIERDA", "DERECHA")
  HIDE          sin parámetros (entrar/salir del escondite)
  AIM           0: fila     1: columna        (el sniper mueve su mira ahí)
  SHOOT         0: fila     1: columna
  REINTENTAR    sin parámetros (después de una derrota, repetir el nivel)

  Servidor -> Cliente
  ESTADO        objeto: EstadoJuego
  MENSAJE       0: emisor   1: texto          (chat, a todos)
  PRIVADO       0: emisor   1: destinatario   2: texto   (solo a esos dos)
  INFO          0: texto                      (aviso solo para ese cliente)
 */

public class Mensaje implements Serializable {
    public String tipo;
    public String[] parametros;
    public Serializable objeto;     // para mandar cosas que no son texto (EstadoJuego)

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

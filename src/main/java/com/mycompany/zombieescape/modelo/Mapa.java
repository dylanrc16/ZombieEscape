/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.modelo;

import java.util.ArrayList;

/**
 *
 * @author dylnr
 */
public class Mapa {
    private String nombre;
    private int deteccion;
    private int cantidadZombies;
    private char[][] mapa;

    public Mapa(String nombre, int deteccion, int cantidadZombies, char[][] mapa) {
        this.nombre = nombre;
        this.deteccion = deteccion;
        this.cantidadZombies = cantidadZombies;
        this.mapa = mapa;
    }

    public String getNombre() { return nombre; }
    public int getDeteccion() { return deteccion; }
    public int getCantidadZombies() { return cantidadZombies; }
    public int getFilas() { return mapa.length; }
    public int getColumnas() { return mapa[0].length; }

    public boolean estaDentro(int f, int c) {
        return f >= 0 && f < getFilas() && c >= 0 && c < getColumnas();
    }

    public char getCelda(int f, int c) {
        return mapa[f][c];
    }

    public boolean esCaminable(int f, int c) {
        if (estaDentro(f, c) == false) 
            return false;
        char prueba = mapa[f][c];
        return prueba != TipoCelda.PARED && prueba != TipoCelda.OBSTACULO;
    }

    public boolean esEscondite(int f, int c) {
        return estaDentro(f, c) && mapa[f][c] == TipoCelda.ESCONDITE;
    }

    public boolean esSalida(int f, int c) {
        return estaDentro(f, c) && mapa[f][c] == TipoCelda.SALIDA;
    }

    // Devuelve todas las posiciones {fila, columna} de un tipo de celda
    public ArrayList<Posicion> buscarCeldas(char tipo) {
    ArrayList<Posicion> lista = new ArrayList();
    for (int f = 0; f < getFilas(); f++)
        for (int c = 0; c < getColumnas(); c++)
            if (mapa[f][c] == tipo)
                lista.add(new Posicion(f, c));
    return lista;
}
}


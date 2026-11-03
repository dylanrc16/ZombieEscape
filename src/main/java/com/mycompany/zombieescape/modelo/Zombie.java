package com.mycompany.zombieescape.modelo;

import java.io.Serializable;
import java.util.Random;

public class Zombie implements IMovible, Serializable {
    private static final Random azar = new Random();
    private static final int PROBABILIDAD_DOBLAR = 20;   // % de veces que dobla en un cruce

    private Posicion posicion;
    private Direccion direccion;
    private boolean vivo = true;
    private int velocidad;                                // milisegundos entre pasos

    public Zombie(Posicion posicion, Direccion direccion, int velocidad) {
        this.posicion = posicion;
        this.direccion = direccion;
        this.velocidad = velocidad;
    }

    public Posicion getPosicion() { return posicion; }
    @Override
    public Direccion getDireccion() { return direccion; }
    public boolean isVivo() { return vivo; }
    public int getVelocidad() { return velocidad; }
    public void morir() { vivo = false; }

    @Override
    public void mover(Mapa mapa) {
        if (!vivo) return;

        // 1. A veces dobla en una esquina o cruce aunque pueda seguir recto
        if (azar.nextInt(100) < PROBABILIDAD_DOBLAR) {
            Direccion lado = azar.nextBoolean() ? direccion.girarDerecha() : direccion.girarIzquierda();
            if (mapa.esCaminable(posicion.siguiente(lado))) {
                direccion = lado;
            }
        }

        // 2. Si choca con pared u obstáculo, gira hacia un lado libre
        if (!mapa.esCaminable(posicion.siguiente(direccion))) {
            Direccion primero = direccion.girarDerecha();
            Direccion segundo = direccion.girarIzquierda();
            if (azar.nextBoolean()) {          // para que no siempre gire al mismo lado
                Direccion aux = primero;
                primero = segundo;
                segundo = aux;
            }
            if (mapa.esCaminable(posicion.siguiente(primero))) {
                direccion = primero;
            } else if (mapa.esCaminable(posicion.siguiente(segundo))) {
                direccion = segundo;
            } else if (mapa.esCaminable(posicion.siguiente(direccion.opuesta()))) {
                direccion = direccion.opuesta();   // callejón sin salida: se devuelve
            } else {
                return;                            // está encerrado
            }
        }

        // 3. Avanza una celda
        posicion = posicion.siguiente(direccion);
    }

    // Mira en línea recta hacia donde camina, hasta el alcance del mapa
    public boolean detecta(Superviviente s, Mapa mapa) {
        if (!vivo || !s.isVivo() || s.isEscondido() || s.isEscapo()) return false;
        if (s.getPosicion().esIgual(posicion)) return true;      // lo alcanzó

        Posicion mirada = posicion;
        for (int paso = 1; paso <= mapa.getDeteccion(); paso++) {
            mirada = mirada.siguiente(direccion);
            if (!mapa.esCaminable(mirada)) return false;           // la pared tapa la vista
            if (s.getPosicion().esIgual(mirada)) return true;
        }
        return false;
    }

    public String getImagen() {
        return "zombie_" + direccion.getNombre();
    }
}

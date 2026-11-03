package com.mycompany.zombieescape.modelo;

// Contrato para todo lo que se desplaza por el mapa (Zombie y Superviviente)
public interface IMovible {
    void mover(Mapa mapa);
    Direccion getDireccion();
}

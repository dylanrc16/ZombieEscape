package com.mycompany.zombieescape.logica;

// Quien quiera enterarse de que la partida cambió (el servidor) implementa esto.
// Así Partida avisa sin saber nada de sockets.
public interface EscuchaPartida {
    void partidaCambio();
}

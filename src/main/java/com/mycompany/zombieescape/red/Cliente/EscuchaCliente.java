package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.red.Models.EstadoJuego;

/**
 * Lo implementa la ventana del juego. Así ThreadCliente le avisa lo que llega
 * del servidor sin saber qué ventana es (igual que EscuchaPartida en el servidor).
 *
 * @author dylnr
 */
public interface EscuchaCliente {
    void mostrarEstado(EstadoJuego estado);   // llegó una foto nueva de la partida
    void mostrarMensaje(String texto);        // chat o aviso del servidor
}

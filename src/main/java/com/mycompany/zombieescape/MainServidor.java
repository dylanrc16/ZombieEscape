package com.mycompany.zombieescape;

import com.mycompany.zombieescape.red.Servidor.ServerFrame;

/**
 * Abre el servidor: carga los escenarios, espera a los jugadores y maneja la partida.
 * Se abre una sola vez, en la computadora que hace de servidor.
 * (Clic derecho sobre esta clase > Run File)
 *
 * @author dylnr
 */
public class MainServidor {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new ServerFrame().setVisible(true));
    }
}

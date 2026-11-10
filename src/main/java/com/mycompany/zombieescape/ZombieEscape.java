/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.zombieescape;

import com.mycompany.zombieescape.gui.Recursos;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Lo que corre NetBeans con "Run Project" (F6), porque así está en el pom.xml.
 * Pregunta si esta PC va a ser el servidor o un jugador.
 *
 * @author dylnr
 */
public class ZombieEscape {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(ZombieEscape::preguntar);
    }

    private static void preguntar() {
        String[] opciones = { "Servidor", "Jugar", "Salir" };
        Image logo = Recursos.imagen("logo_dd");
        ImageIcon icono = logo == null ? null : new ImageIcon(logo);

        int elegida = JOptionPane.showOptionDialog(null,
                "<html><b>¿Qué quiere abrir en esta computadora?</b><br><br>"
                + "<b>Servidor:</b> la partida y el chat (se abre una sola vez).<br>"
                + "<b>Jugar:</b> el login para entrar como superviviente o sniper.</html>",
                "Zombi Escape", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                icono, opciones, opciones[1]);

        if (elegida == 0) {
            MainServidor.main(new String[0]);
        } else if (elegida == 1) {
            MainCliente.main(new String[0]);
        }
        // "Salir" o cerrar la ventanita: no se abre nada y el programa termina
    }
}

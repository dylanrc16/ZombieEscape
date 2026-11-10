/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape;

import com.mycompany.zombieescape.red.Cliente.VentanaLogin;

/**
 * Abre el juego para un jugador: la ventana de login.
 * (Clic derecho sobre esta clase > Run File)
 *
 * @author dylnr
 */
public class MainCliente {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}

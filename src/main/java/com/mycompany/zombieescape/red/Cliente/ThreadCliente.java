/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.red.Models.EstadoJuego;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 *
 * @author diego
 */
public class ThreadCliente extends Thread{
    private boolean isRunning = true;
    private Cliente cliente;
    private Socket socket;
    private ObjectInputStream lectorDeObjetos;


    public ThreadCliente(Cliente cliente, Socket socket) {

        try {
            this.cliente = cliente;
            this.socket = socket;
            lectorDeObjetos = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.getLogger(ThreadCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    public void run (){

        while (isRunning){

            try {
                Mensaje msg = (Mensaje)lectorDeObjetos.readObject();
                //la ventana del juego todavía puede no existir (paso 4)
                EscuchaCliente escucha = cliente.getEscucha();
                if (escucha == null) continue;

                if (msg.tipo.equals("ESTADO")){
                    //foto nueva de la partida: la ventana la dibuja
                    escucha.mostrarEstado((EstadoJuego) msg.objeto);
                }else if (msg.tipo.equals("MENSAJE")){
                    //pintar el mensaje recibido en el chat
                    String emisor = msg.parametros[0];
                    String texto = msg.parametros[1];
                    escucha.mostrarMensaje(emisor + ": " + texto);
                }else if (msg.tipo.equals("INFO")){
                    //aviso del servidor solo para mí (resultado del disparo, bienvenida...)
                    escucha.mostrarMensaje("[Servidor] " + msg.parametros[0]);
                }

            } catch (IOException ex) {
                isRunning = false;
                EscuchaCliente escucha = cliente.getEscucha();
                if (escucha != null) escucha.mostrarMensaje("[Servidor] Se perdió la conexión");
            } catch (ClassNotFoundException ex) {
                System.getLogger(ThreadCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }



        }

    }
}

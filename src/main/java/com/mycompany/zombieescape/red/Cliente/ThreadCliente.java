/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.red.Models.EstadoJuego;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.ByteArrayInputStream;
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


    //throws: si el servidor no responde, Cliente se entera y el login muestra el error
    public ThreadCliente(Cliente cliente, Socket socket) throws IOException {
        this.cliente = cliente;
        this.socket = socket;
        lectorDeObjetos = new ObjectInputStream(socket.getInputStream());
    }
    //el servidor manda la foto empaquetada en bytes: aquí se vuelve a armar el EstadoJuego
    private EstadoJuego desempaquetar(byte[] foto) throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(foto))) {
            return (EstadoJuego) entrada.readObject();
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
                    escucha.mostrarEstado(desempaquetar((byte[]) msg.objeto));
                }else if (msg.tipo.equals("MENSAJE")){
                    //pintar el mensaje recibido en el chat
                    String emisor = msg.parametros[0];
                    String texto = msg.parametros[1];
                    escucha.mostrarMensaje(emisor + ": " + texto);
                }else if (msg.tipo.equals("PRIVADO")){
                    //mensaje privado: lo ven solo el que lo mandó y el que lo recibe
                    escucha.mostrarMensaje("[Privado] " + msg.parametros[0] + " → "
                            + msg.parametros[1] + ": " + msg.parametros[2]);
                }else if (msg.tipo.equals("RECHAZO")){
                    //no nos dejaron entrar: la ventana vuelve al login con el motivo
                    isRunning = false;
                    escucha.conexionRechazada(msg.parametros[0]);
                }else if (msg.tipo.equals("INFO")){
                    //aviso del servidor solo para mí (resultado del disparo, bienvenida...)
                    escucha.mostrarMensaje("[Servidor] " + msg.parametros[0]);
                }

            } catch (IOException ex) {
                if (!isRunning) break;   //ya se había terminado (por ejemplo, después de un RECHAZO)
                isRunning = false;
                EscuchaCliente escucha = cliente.getEscucha();
                if (escucha != null) escucha.mostrarMensaje("[Servidor] Se perdió la conexión");
            } catch (ClassNotFoundException ex) {
                System.getLogger(ThreadCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }



        }

    }
}
